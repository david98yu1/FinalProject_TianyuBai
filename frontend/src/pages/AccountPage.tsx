import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api, parseJwtClaims } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { type AccountResponse, type AddressRequest} from '../api/client'

export type PaymentMethodDto = {
  id?: number;
  brand?: string; // e.g., VISA
  last4?: string;
  expMonth?: number;
  expYear?: number;
};

export type UpdateAccountRequest = {
  email: string;
  username: string;
};

// Pretty label helper
const Label: React.FC<React.PropsWithChildren<{ htmlFor?: string }>> = ({ htmlFor, children }) => (
    <label htmlFor={htmlFor} className="block text-sm font-medium text-gray-700 mb-1">
      {children}
    </label>
);

export default function AccountPage() {
  const nav = useNavigate();
  const { logout } = useAuth();

  const [accountId, setAccountId] = useState<number | null>(null);
  const [me, setMe] = useState<AccountResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  // Form state for profile
  const [email, setEmail] = useState("");
  const [username, setUsername] = useState("");
  const [saving, setSaving] = useState(false);

  // Form state for new address
  type AddressForm = AddressRequest & { zip?: string; type?: string };
  const [addr, setAddr] = useState<AddressForm>({ line1: "", city: "", state: "", zip: "", country: "", type: "SHIPPING" });
  const [addrSaving, setAddrSaving] = useState(false);

  const token = useMemo(() => localStorage.getItem("jwt"), []);

  // ----------- Load identity from token and then fetch account -----------
  useEffect(() => {
    if (!token) {
      nav("/login", { replace: true });
      return;
    }

    const claims = parseJwtClaims(token);

    // Try to derive the numeric Account ID from the token first
    // (if your Account.id != auth user id, backend will still 403; see notes below)
    let id: number | null = null;
    if (claims) {
      // subject as numeric id
      if (typeof claims.sub === "string" && /^\d+$/.test(claims.sub)) id = Number(claims.sub);
      // custom fields some auth servers use
      if (typeof (claims as any).id === "number") id = (claims as any).id;
      if (typeof (claims as any).userId === "number") id = (claims as any).userId;
    }

    setAccountId(id);
  }, [token, nav]);

  useEffect(() => {
    let alive = true;
    if (accountId == null) {
      setLoading(false);
      return;
    }

    setLoading(true);
    setErr(null);
    api.accounts
        .getById(accountId)
        .then((data: AccountResponse) => {
          if (!alive) return;
          setMe(data);
          setEmail(data.email);
          setUsername(data.username);
        })
        .catch((e: any) => {
          if (!alive) return;
          const status = e?.response?.status;
          if (status === 401 || status === 403) {
            // Not authorized to view this account. Most common causes:
            // 1) Missing/invalid Authorization header
            // 2) The numeric id from token != Account.id -> security check denies
            setErr("Forbidden: you are not allowed to access this account record.");
          } else {
            setErr(e?.message || "Failed to load account");
          }
        })
        .finally(() => alive && setLoading(false));

    return () => {
      alive = false;
    };
  }, [accountId]);

  // ----------- Actions -----------
  const onSaveProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!me) return;
    try {
      setSaving(true);
      const updated = await api.accounts.update(me.id, { email, username } as UpdateAccountRequest);
      setMe(updated);
      setErr(null);
    } catch (e: any) {
      setErr(e?.response?.data?.message || e?.message || "Failed to update account");
    } finally {
      setSaving(false);
    }
  };

  const onAddAddress = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!me) return;

    // client-side validation to avoid NotBlank/NotNull from backend
    const errors: string[] = [];
    if (!addr.line1?.trim()) errors.push("Address line 1 is required.");
    if (!addr.city?.trim()) errors.push("City is required.");
    if (!addr.zip?.trim()) errors.push("ZIP is required.");
    if (!(addr as any).type) errors.push("Type is required.");

    if (errors.length) {
      setErr(errors.join(" "));
      return;
    }

    // Trim payload and avoid sending empty strings for nullable fields
    const payload: AddressRequest = {
      recipient: addr.recipient?.trim() || undefined,
      line1: addr.line1.trim(),
      line2: addr.line2?.trim() || undefined,
      city: addr.city.trim(),
      state: addr.state?.trim() || undefined,
      zip: addr.zip!.trim(),
      country: addr.country?.trim() || undefined,
      phone: addr.phone?.trim() || undefined,
      type: (addr as any).type as any,
    };

    try {
      setAddrSaving(true);
      await api.accounts.addAddress(me.id, payload);
      const freshAfterAddr = await api.accounts.getById(me.id);
      setMe(freshAfterAddr);
      setAddr({ line1: "", city: "", state: "", zip: "", country: "", type: "SHIPPING" } as any);
      setErr(null);
    } catch (e: any) {
      setErr(e?.response?.data?.message || e?.message || "Failed to add address");
    } finally {
      setAddrSaving(false);
    }
  };

  if (!token) return <div style={{ padding: 24 }}>Redirecting to login…</div>;
  if (loading) return <div style={{ padding: 24 }}>Loading…</div>;

  return (
      <div className="max-w-3xl mx-auto p-6">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-semibold">My Account</h1>
          <button onClick={() => { logout(); nav("/login", { replace: true }); }} className="px-3 py-1.5 rounded bg-gray-100 hover:bg-gray-200">
            Sign out
          </button>
        </div>

        {err && (
            <div className="mb-4 p-3 rounded border border-red-300 bg-red-50 text-sm text-red-800">{err}</div>
        )}

        {!me ? (
            <div className="text-sm text-gray-600">
              We couldn't fetch your account by ID from the token. Your token may not contain the Account primary key.
            </div>
        ) : (
            <>
              <section className="mb-8">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <div className="text-sm text-gray-500">Account ID</div>
                    <div className="font-mono">{me.id}</div>
                  </div>
                  <div>
                    <div className="text-sm text-gray-500">Auth User ID</div>
                    <div className="font-mono">{me.authUserId}</div>
                  </div>
                </div>
              </section>

              <section className="mb-10">
                <h2 className="text-lg font-medium mb-3">Profile</h2>
                <form onSubmit={onSaveProfile} className="space-y-4">
                  <div>
                    <Label htmlFor="email">Email</Label>
                    <input
                        id="email"
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className="w-full border rounded px-3 py-2"
                        required
                    />
                  </div>
                  <div>
                    <Label htmlFor="username">Username</Label>
                    <input
                        id="username"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        className="w-full border rounded px-3 py-2"
                        required
                    />
                  </div>
                  <div className="pt-2">
                    <button
                        type="submit"
                        disabled={saving}
                        className="px-4 py-2 rounded bg-black text-white disabled:opacity-60"
                    >
                      {saving ? "Saving…" : "Save changes"}
                    </button>
                  </div>
                </form>
              </section>

              <section className="mb-10">
                <h2 className="text-lg font-medium mb-3">Addresses</h2>
                {me.addresses?.length ? (
                    <ul className="space-y-2 mb-4">
                      {me.addresses.map((a, i) => (
                          <li key={a.id ?? i} className="border rounded p-3">
                            <div className="font-medium">{a.recipient || "Recipient"}</div>
                            <div className="text-sm text-gray-700">
                              {[a.line1, a.line2].filter(Boolean).join(", ")}
                            </div>
                            <div className="text-sm text-gray-700">
                              {[a.city, a.state, a.zip].filter(Boolean).join(", ")}
                            </div>
                            <div className="text-sm text-gray-700">{a.country}</div>
                          </li>
                      ))}
                    </ul>
                ) : (
                    <div className="text-sm text-gray-600 mb-4">No addresses yet.</div>
                )}

                <form onSubmit={onAddAddress} className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <Label>Recipient</Label>
                    <input className="w-full border rounded px-3 py-2" value={addr.recipient || ""} onChange={(e) => setAddr({ ...addr, recipient: e.target.value })} />
                  </div>
                  <div>
                    <Label>Phone</Label>
                    <input className="w-full border rounded px-3 py-2" value={addr.phone || ""} onChange={(e) => setAddr({ ...addr, phone: e.target.value })} />
                  </div>
                  <div className="md:col-span-2">
                    <Label>Address line 1</Label>
                    <input className="w-full border rounded px-3 py-2" value={addr.line1} onChange={(e) => setAddr({ ...addr, line1: e.target.value })} required />
                  </div>
                  <div className="md:col-span-2">
                    <Label>Address line 2</Label>
                    <input className="w-full border rounded px-3 py-2" value={addr.line2 || ""} onChange={(e) => setAddr({ ...addr, line2: e.target.value })} />
                  </div>
                  <div>
                    <Label>City</Label>
                    <input className="w-full border rounded px-3 py-2" value={addr.city} onChange={(e) => setAddr({ ...addr, city: e.target.value })} required />
                  </div>
                  <div>
                    <Label>State</Label>
                    <input className="w-full border rounded px-3 py-2" value={addr.state || ""} onChange={(e) => setAddr({ ...addr, state: e.target.value })} />
                  </div>
                  <div>
                    <Label>Postal code</Label>
                    <input className="w-full border rounded px-3 py-2" value={addr.zip || ""} onChange={(e) => setAddr({ ...addr, zip: e.target.value })} />
                  </div>
                  <div>
                    <Label>Type</Label>
                    <select className="w-full border rounded px-3 py-2" value={(addr as any).type || ""} onChange={(e) => setAddr({ ...addr, type: e.target.value as any })} required>
                      <option value="">Select type</option>
                      <option value="SHIPPING">SHIPPING</option>
                      <option value="BILLING">BILLING</option>
                    </select>
                  </div>
                  <div>
                    <Label>Country</Label>
                    <input className="w-full border rounded px-3 py-2" value={addr.country || ""} onChange={(e) => setAddr({ ...addr, country: e.target.value })} />
                  </div>
                  <div className="md:col-span-2 pt-2">
                    <button type="submit" disabled={addrSaving} className="px-4 py-2 rounded bg-black text-white disabled:opacity-60">
                      {addrSaving ? "Adding…" : "Add address"}
                    </button>
                  </div>
                </form>
              </section>
            </>
        )}
      </div>
  );
}
