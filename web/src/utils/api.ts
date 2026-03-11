const DEFAULT_API_URL = 'http://localhost:8080';

const normalizeBaseUrl = (value?: string): string => {
    const raw = value?.trim();
    if (!raw) {
        return DEFAULT_API_URL;
    }
    return raw.replace(/\/+$/, '');
};

export const API_BASE_URL = normalizeBaseUrl(process.env.REACT_APP_API_URL);

export const buildApiUrl = (path: string): string => {
    const normalizedPath = path.startsWith('/') ? path : `/${path}`;
    return `${API_BASE_URL}${normalizedPath}`;
};
