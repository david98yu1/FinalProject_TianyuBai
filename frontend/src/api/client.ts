/// <reference types="vite/client" />

// src/api/client.ts

// -----------------------------
// Types
// -----------------------------
export type Page<T> = { content: T[]; totalPages: number; totalElements: number };
export type Item = {
  id: number; sku: string; name: string; description?: string;
  price: number; stock: number; pictureUrl?: string;
};
export type OrderItemReq = { sku: string; quantity: number };
export type Order = {
  id: number; status: string; total: number;
  items: Array<{ sku: string; name?: string; quantity: number; lineTotal: number }>;
};
export type RegisterResponse = {
  token: string;
  // userId?: string;     // optional
};
export type CreateAccountRequest = {
  authUserId: string;
  email: string;
  username: string;
};
export type AccountResponse = {
  id: number;
  authUserId: string;
  email: string;
  username: string;
  addresses?: AddressDto[];
  // paymentMethods?: PaymentMethodDto[];
};

export type AddressDto = {
  id: number;
  recipient?: string;
  line1: string;
  line2?: string;
  city: string;
  state?: string;
  zip?: string;
  country?: string;
  phone?: string;
  type: 'SHIPPING' | 'BILLING';
};

export type AddressRequest = Omit<AddressDto, "id">;

// -----------------------------
// Mode & Origins
// DEV (vite dev server): use proxy via relative paths
// PROD (vite build/preview or your deployment): use absolute URLs from env
// -----------------------------
const USE_VITE_PROXY = import.meta.env.DEV;

const AUTH_ORIGIN     = USE_VITE_PROXY ? "" : (import.meta.env.VITE_AUTH_BASE_URL     ?? "http://localhost:9000");
const ACCOUNT_ORIGIN  = USE_VITE_PROXY ? "" : (import.meta.env.VITE_ACCOUNT_BASE_URL  ?? "http://localhost:9001");
const ITEM_ORIGIN     = USE_VITE_PROXY ? "" : (import.meta.env.VITE_ITEM_BASE_URL     ?? "http://localhost:9002");
const ORDER_ORIGIN    = USE_VITE_PROXY ? "" : (import.meta.env.VITE_ORDER_BASE_URL    ?? "http://localhost:9003");
const PAYMENT_ORIGIN  = USE_VITE_PROXY ? "" : (import.meta.env.VITE_PAYMENT_BASE_URL  ?? "http://localhost:9004");

// Paths used by the proxy (match vite.config.ts)
const AUTH_PATH     = "/auth";
const ACCOUNTS_PATH = "/accounts";
const ITEMS_PATH    = "/items";
const ORDERS_PATH   = "/orders";
const PAYMENTS_PATH = "/payments";

// Robust join that avoids `//` and missing `/`
function join(base: string, path: string) {
  if (!base) return path.startsWith("/") ? path : `/${path}`;
  return `${base.replace(/\/+$/,"")}/${path.replace(/^\/+/, "")}`;
}

// -----------------------------
// Token & JWT helpers
// -----------------------------
function token() {
  return localStorage.getItem("jwt");
}

function b64urlToB64(s: string) {
  let out = s.replace(/-/g, "+").replace(/_/g, "/");
  while (out.length % 4) out += "=";
  return out;
}

export function parseJwtClaims(tok: string | null): any | null {
  if (!tok) return null;
  const parts = tok.split(".");
  if (parts.length < 2) return null;
  try {
    const json = atob(b64urlToB64(parts[1]));
    return JSON.parse(json);
  } catch {
    return null;
  }
}

export function getAccountIdFromToken(tok: string | null): number | null {
  const c = parseJwtClaims(tok);
  if (!c) return null;
  if (typeof c.id === "number") return c.id;
  if (typeof c.userId === "number") return c.userId;
  if (typeof c.sub === "string" && /^\d+$/.test(c.sub)) return Number(c.sub);
  return null;
}

async function request<T>(url: string, init?: RequestInit): Promise<T> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(init?.headers ? (init.headers as Record<string, string>) : {}),
  };
  const t = token();
  if (t) headers["Authorization"] = `Bearer ${t}`;

  // guard against accidental "undefined/foo"
  if (!url.startsWith("/http") && !url.startsWith("http") && !url.startsWith("/")) {
    url = `/${url}`;
  }

  const res = await fetch(url, { ...init, headers });
  if (!res.ok) {
    const text = await res.text();
    try {
      const j = JSON.parse(text);
      throw new Error(j.message || text || `HTTP ${res.status}`);
    } catch {
      throw new Error(text || `HTTP ${res.status}`);
    }
  }
  if (res.status === 204) return undefined as unknown as T;
  return res.json() as Promise<T>;
}

// Query string helper
function qs(params: Record<string, string | number | undefined>) {
  const p = new URLSearchParams();
  for (const [k, v] of Object.entries(params)) {
    if (v !== undefined && v !== null) p.set(k, String(v));
  }
  const s = p.toString();
  return s ? `?${s}` : "";
}

function saveToken(token: string, expiresAt?: string) {
  localStorage.setItem("jwt", token);
  if (expiresAt) localStorage.setItem("jwt_expiresAt", expiresAt);
}

// -----------------------------
// API
// -----------------------------
export const api = {
  auth: {
    async login(login: string, password: string) {
      const url = join(AUTH_ORIGIN, `${AUTH_PATH}/login`);
      const res = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ login, password }),
      });

      if (!res.ok) {
        const txt = await res.text();
        throw new Error(txt || `HTTP ${res.status}`);
      }

      // Parse body
      let body: any = {};
      try { body = await res.json(); } catch { body = {}; }

      // Accept common token field names and/or Authorization header
      const fromBody =
          body?.token ??
          body?.accessToken ??
          body?.access_token ??
          body?.jwt ??
          body?.id_token ??
          body?.idToken;

      const authHeader =
          res.headers.get("Authorization") ??
          res.headers.get("authorization") ??
          res.headers.get("X-Auth-Token") ??
          res.headers.get("x-auth-token");

      const fromHeader = authHeader?.replace(/^Bearer\s+/i, "");

      const token = fromBody || fromHeader;
      if (!token) throw new Error("No token returned.");

      // Persist token (so request() will send Authorization next time)
      saveToken(token, body?.expiresAt);

      // Return full body plus normalized token for caller convenience
      return { ...body, token };
    },
    register(email: string, username: string, password: string) {
      return request<{ token: string }>(join(AUTH_ORIGIN, `${AUTH_PATH}/register`), {
        method: "POST",
        body: JSON.stringify({ email, username, password }),
      });
    },
  },

  accounts: {
    getById(id: number) {
      return request<any>(join(ACCOUNT_ORIGIN, `${ACCOUNTS_PATH}/${id}`));
    },
    createAccount(body: CreateAccountRequest, token: string) {
      return request<AccountResponse>(
          join(ACCOUNT_ORIGIN, `${ACCOUNTS_PATH}`),
          {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify(body),
          }
      );
    },
    update(id: number, body: { email: string; username: string }) {
      return request<AccountResponse>(join(ACCOUNT_ORIGIN, `${ACCOUNTS_PATH}/${id}`), {
        method: "PUT",
        body: JSON.stringify(body),
      });
    },
    addAddress(id: number, body: AddressRequest) {
      return request<AddressDto[]>(join(ACCOUNT_ORIGIN, `${ACCOUNTS_PATH}/${id}/addresses`), {
        method: "POST",
        body: JSON.stringify(body),
      });
    }
  },



  items: {
    search<T = Page<Item>>(q?: string, page = 0, size = 12) {
      return request<T>(join(ITEM_ORIGIN, `${ITEMS_PATH}${qs({ q, page, size })}`));
    },
    get(id: number) {
      return request<Item>(join(ITEM_ORIGIN, `${ITEMS_PATH}/${id}`));
    },

    // NEW: create
    create(data: Omit<Item, "id">) {
      return request<Item>(join(ITEM_ORIGIN, ITEMS_PATH), {
        method: "POST",
        body: JSON.stringify(data),
      });
    },

    // NEW: update
    update(id: number, data: Partial<Omit<Item, "id">>) {
      return request<Item>(join(ITEM_ORIGIN, `${ITEMS_PATH}/${id}`), {
        method: "PUT",
        body: JSON.stringify(data),
      });
    },
  },

  orders: {
    async create(accountId: number, items: OrderItemReq[]) {
      try {
        return await request<Order>(join(ORDER_ORIGIN, ORDERS_PATH), {
          method: "POST",
          body: JSON.stringify({ accountId, items }),
        });
      } catch {
        // if server infers account, retry without accountId
        return request<Order>(join(ORDER_ORIGIN, ORDERS_PATH), {
          method: "POST",
          body: JSON.stringify({ items }),
        });
      }
    },
    get(id: number) {
      return request<Order>(join(ORDER_ORIGIN, `${ORDERS_PATH}/${id}`));
    },
    cancel(id: number) {
      return request<Order>(join(ORDER_ORIGIN, `${ORDERS_PATH}/${id}/cancel`), { method: "POST" });
    },
    confirm(id: number) {
      return request<Order>(join(ORDER_ORIGIN, `${ORDERS_PATH}/${id}/confirm`), { method: "POST" });
    },
  },

  payments: {
    pay(orderId: number, amount: number) {
      return request<any>(join(PAYMENT_ORIGIN, `${PAYMENTS_PATH}`), {
        method: "POST",
        body: JSON.stringify({ orderId, amount }),
      });
    },
    get(id: number) {
      return request<any>(join(PAYMENT_ORIGIN, `${PAYMENTS_PATH}/${id}`));
    },
  },
};
