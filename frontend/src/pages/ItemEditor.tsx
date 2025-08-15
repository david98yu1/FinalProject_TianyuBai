import React, { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {api, type Item} from "../api/client";

type FormState = {
    sku: string;
    name: string;
    description?: string;
    price: string; // keep as string for input control, convert to number on save
    stock: string; // same idea
    pictureUrl?: string;
};

const emptyForm: FormState = {
    sku: "",
    name: "",
    description: "",
    price: "",
    stock: "",
    pictureUrl: "",
};

export default function ItemEditor() {
    const { id } = useParams<{ id: string }>();
    const isEdit = useMemo(() => !!id, [id]);
    const navigate = useNavigate();

    const [form, setForm] = useState<FormState>(emptyForm);
    const [loading, setLoading] = useState<boolean>(!!isEdit);
    const [saving, setSaving] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    // Load existing item when editing
    useEffect(() => {
        let alive = true;
        async function load() {
            try {
                if (!isEdit) return;
                setLoading(true);
                const item = await api.items.get(Number(id));
                if (!alive) return;
                setForm({
                    sku: item.sku ?? "",
                    name: item.name ?? "",
                    description: item.description ?? "",
                    price: item.price != null ? String(item.price) : "",
                    stock: item.stock != null ? String(item.stock) : "",
                    pictureUrl: item.pictureUrl ?? "",
                });
            } catch (e: any) {
                if (!alive) return;
                setError(e?.message || "Failed to load item");
            } finally {
                if (alive) setLoading(false);
            }
        }
        load();
        return () => { alive = false; };
    }, [isEdit, id]);

    function onChange<K extends keyof FormState>(key: K, val: FormState[K]) {
        setForm(f => ({ ...f, [key]: val }));
    }

    function validate(): string | null {
        if (!form.sku.trim()) return "SKU is required";
        if (!form.name.trim()) return "Name is required";
        if (form.price.trim() === "" || Number.isNaN(Number(form.price))) return "Price must be a number";
        if (form.stock.trim() === "" || Number.isNaN(Number(form.stock))) return "Stock must be a number";
        if (Number(form.price) < 0) return "Price cannot be negative";
        if (Number(form.stock) < 0) return "Stock cannot be negative";
        return null;
    }

    async function onSubmit(e: React.FormEvent) {
        e.preventDefault();
        setError(null);
        const err = validate();
        if (err) {
            setError(err);
            return;
        }

        const payload: Omit<Item, "id"> = {
            sku: form.sku.trim(),
            name: form.name.trim(),
            description: form.description?.trim() || undefined,
            price: Number(form.price),
            stock: Number(form.stock),
            pictureUrl: form.pictureUrl?.trim() || undefined,
        };

        try {
            setSaving(true);
            if (isEdit) {
                await api.items.update(Number(id), payload);
                alert("Item updated");
            } else {
                const created = await api.items.create(payload);
                alert("Item created");
                // navigate to edit page for convenience
                navigate(`/admin/items/${created.id}`);
                return;
            }
            // go back to some list page if you have one
            // navigate("/admin/items");
        } catch (e: any) {
            setError(e?.message || "Save failed");
        } finally {
            setSaving(false);
        }
    }

    if (loading) return <div style={{ padding: 16 }}>Loading item...</div>;

    return (
        <div style={{ maxWidth: 720, margin: "24px auto", padding: 16 }}>
            <h2 style={{ marginBottom: 16 }}>{isEdit ? "Edit Item" : "Create Item"}</h2>

            {error && (
                <div style={{ background: "#fee", border: "1px solid #f99", padding: 12, marginBottom: 16 }}>
                    {error}
                </div>
            )}

            <form onSubmit={onSubmit}>
                <div style={{ display: "grid", gap: 12 }}>
                    <label>
                        <div>SKU *</div>
                        <input
                            value={form.sku}
                            onChange={(e) => onChange("sku", e.target.value)}
                            placeholder="e.g. ABC-123"
                            required
                        />
                    </label>

                    <label>
                        <div>Name *</div>
                        <input
                            value={form.name}
                            onChange={(e) => onChange("name", e.target.value)}
                            placeholder="Item name"
                            required
                        />
                    </label>

                    <label>
                        <div>Description</div>
                        <textarea
                            value={form.description}
                            onChange={(e) => onChange("description", e.target.value)}
                            rows={4}
                            placeholder="Optional description"
                        />
                    </label>

                    <label>
                        <div>Price *</div>
                        <input
                            type="number"
                            step="0.01"
                            min="0"
                            value={form.price}
                            onChange={(e) => onChange("price", e.target.value)}
                            placeholder="0.00"
                            required
                        />
                    </label>

                    <label>
                        <div>Stock *</div>
                        <input
                            type="number"
                            step="1"
                            min="0"
                            value={form.stock}
                            onChange={(e) => onChange("stock", e.target.value)}
                            placeholder="0"
                            required
                        />
                    </label>

                    <label>
                        <div>Picture URL</div>
                        <input
                            value={form.pictureUrl}
                            onChange={(e) => onChange("pictureUrl", e.target.value)}
                            placeholder="https://..."
                        />
                    </label>
                </div>

                <div style={{ marginTop: 20, display: "flex", gap: 8 }}>
                    <button type="submit" disabled={saving}>
                        {saving ? "Saving..." : isEdit ? "Update Item" : "Create Item"}
                    </button>
                    <button type="button" onClick={() => navigate(-1)} disabled={saving}>
                        Cancel
                    </button>
                </div>
            </form>
        </div>
    );
}
