const DEFAULT_TENANT_ID = 'chipcook';

const normalizeTenantId = (raw: string): string | null => {
    const normalized = raw.trim().toLowerCase().replace(/-/g, '_');
    if (!normalized || !/^[a-z0-9_]+$/.test(normalized)) {
        return null;
    }
    return normalized;
};

export const resolveTenantIdFromHostname = (hostname: string): string => {
    if (!hostname) {
        return DEFAULT_TENANT_ID;
    }

    const host = hostname.toLowerCase();
    if (host === 'localhost' || host === '127.0.0.1') {
        return DEFAULT_TENANT_ID;
    }

    if (host.endsWith('.localhost')) {
        const subdomain = host.slice(0, -'.localhost'.length).split('.')[0];
        return normalizeTenantId(subdomain) ?? DEFAULT_TENANT_ID;
    }

    const parts = host.split('.');
    if (parts.length >= 3) {
        return normalizeTenantId(parts[0]) ?? DEFAULT_TENANT_ID;
    }

    return DEFAULT_TENANT_ID;
};

export const getTenantId = (): string => {
    if (typeof window === 'undefined') {
        return DEFAULT_TENANT_ID;
    }
    return resolveTenantIdFromHostname(window.location.hostname);
};

export const withTenantHeader = (headers: Record<string, string> = {}): Record<string, string> => ({
    ...headers,
    'X-Tenant-ID': getTenantId()
});
