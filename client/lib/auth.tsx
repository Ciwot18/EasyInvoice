/**
 * Authentication context – provides user session, login/logout, and
 * role-based permission helpers to the entire application.
 *
 * AUTH_BYPASS (hard-coded to `true` below) injects a mock session so
 * the app is usable in the v0 preview without a live backend.
 *
 * When the real backend is wired up set AUTH_BYPASS = false and the
 * real API layer will be lazily loaded from '@/lib/api-shim'.
 */

'use client';

import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  useCallback,
  useRef,
  type ReactNode,
} from 'react';
import type { User, UserRole, AuthState } from './types';


const AUTH_BYPASS = (import.meta.env.VITE_AUTH_BYPASS as string | undefined) === 'true';


const ROLE_KEY = 'ei_bypass_role';

function getPersistedRole(): UserRole {
  if (typeof window === 'undefined') return 'company_manager';
  try {
    const stored = window.sessionStorage.getItem(ROLE_KEY);
    if (stored === 'platform_admin' || stored === 'company_manager' || stored === 'back_office_operator') {
      return stored;
    }
  } catch {  }
  return 'company_manager';
}

function persistRole(role: UserRole) {
  try { window.sessionStorage.setItem(ROLE_KEY, role); } catch {  }
}


const MOCK_USERS: Record<UserRole, User> = {
  company_manager: {
    id: 'mock_cm',
    email: 'manager@acmesolutions.com',
    name: 'Michael Chen',
    role: 'company_manager',
    companyId: 'comp_1',
    companyName: 'Acme Solutions Ltd',
    assignedClientIds: [],
    createdAt: '2024-02-01T09:00:00Z',
  },
  back_office_operator: {
    id: 'mock_bo',
    email: 'operator@acmesolutions.com',
    name: 'Emily Davis',
    role: 'back_office_operator',
    companyId: 'comp_1',
    companyName: 'Acme Solutions Ltd',
    assignedClientIds: ['client_1', 'client_2', 'client_3'],
    createdAt: '2024-03-10T14:00:00Z',
  },
  platform_admin: {
    id: 'mock_pa',
    email: 'admin@easyinvoice.io',
    name: 'Sarah Johnson',
    role: 'platform_admin',
    companyId: '',
    companyName: undefined,
    assignedClientIds: [],
    createdAt: '2024-01-15T10:00:00Z',
  },
};

const TOKEN_KEY = 'ei_token';
const AUTH_ME_TIMEOUT_MS = Number(import.meta.env.VITE_AUTH_ME_TIMEOUT_MS ?? 10000);


type AuthProfile = {
  id: number;
  email: string;
  name: string;
  role: string;
  enabled: boolean;
  createdAt: string;
};

function profileToUser(p: AuthProfile): User {
  const roleMap: Record<string, UserRole> = {
    PLATFORM_ADMIN: 'platform_admin',
    COMPANY_MANAGER: 'company_manager',
    BACK_OFFICE: 'back_office_operator',
  };
  return {
    id: String(p.id),
    email: p.email,
    name: p.name || p.email,
    role: roleMap[p.role] ?? 'back_office_operator',
    companyId: '',
    assignedClientIds: [],
    createdAt: p.createdAt,
  };
}

function withTimeout<T>(promise: Promise<T>, timeoutMs: number): Promise<T> {
  return new Promise<T>((resolve, reject) => {
    const timeoutId = window.setTimeout(() => {
      reject(new Error(`Timeout after ${timeoutMs}ms`));
    }, timeoutMs);
    promise
      .then((value) => {
        window.clearTimeout(timeoutId);
        resolve(value);
      })
      .catch((err) => {
        window.clearTimeout(timeoutId);
        reject(err);
      });
  });
}


const AuthContext = createContext<AuthState | null>(null);


export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(
    AUTH_BYPASS ? MOCK_USERS[getPersistedRole()] : null,
  );
  const [isLoading, setIsLoading] = useState(!AUTH_BYPASS);
  const didBootstrap = useRef(AUTH_BYPASS); // true = skip effect body

  
  const switchRole = useCallback((role: UserRole) => {
    if (!AUTH_BYPASS) return;
    persistRole(role);
    setUser(MOCK_USERS[role]);
  }, []);

  
  const login = useCallback(async (email: string, password: string, companyId?: number | null): Promise<boolean> => {
    if (AUTH_BYPASS) {
      const r = getPersistedRole();
      persistRole(r);
      setUser(MOCK_USERS[r]);
      return true;
    }
    try {
      const mod = await import('@/lib/api-shim');
      const resp = await mod.services.auth.login({ email, password, companyId });
      window.localStorage.setItem(TOKEN_KEY, resp.token);
      const me = await withTimeout(mod.services.auth.me(), AUTH_ME_TIMEOUT_MS);
      setUser(profileToUser(me));
      return true;
    } catch {
      return false;
    }
  }, []);

  
  const logout = useCallback(() => {
    window.localStorage.removeItem(TOKEN_KEY);
    setUser(null);
  }, []);

  
  useEffect(() => {
    if (didBootstrap.current) return;
    didBootstrap.current = true;
    let alive = true;
    (async () => {
      const token = window.localStorage.getItem(TOKEN_KEY);
      if (!token) { if (alive) setIsLoading(false); return; }
      try {
        const mod = await import('@/lib/api-shim');
        const me = await withTimeout(mod.services.auth.me(), AUTH_ME_TIMEOUT_MS);
        if (alive) setUser(profileToUser(me));
      } catch {
        window.localStorage.removeItem(TOKEN_KEY);
        if (alive) setUser(null);
      } finally {
        if (alive) setIsLoading(false);
      }
    })();
    return () => { alive = false; };
  }, []);

  
  useEffect(() => {
    if (AUTH_BYPASS) return;
    let off: (() => void) | undefined;
    import('@/lib/api-shim').then(({ onApiError }) => {
      off = onApiError((d) => { if (d.status === 401) logout(); });
    });
    return () => off?.();
  }, [logout]);

  
  const value = useMemo<AuthState>(
    () => ({ user, isAuthenticated: !!user, isLoading, login, logout, switchRole }),
    [user, isLoading, login, logout, switchRole],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}


export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}

export function isAuthBypassed(): boolean {
  return AUTH_BYPASS;
}

export function usePermissions() {
  const { user } = useAuth();

  const isPlatformAdmin    = user?.role === 'platform_admin';
  const isCompanyManager   = user?.role === 'company_manager';
  const isBackOfficeOperator = user?.role === 'back_office_operator';

  const canManageUsers       = isCompanyManager;
  const canManageCompany     = isCompanyManager;
  const canAccessAllClients  = isCompanyManager;
  const canManageCompanies   = isPlatformAdmin;
  const canProvisionUsers    = isPlatformAdmin;
  const canViewMonitoring    = isPlatformAdmin;
  const canAccessCompanyData = isCompanyManager || isBackOfficeOperator;

  const canAccessClient = (clientId: string) => {
    if (!user || isPlatformAdmin) return false;
    if (canAccessAllClients) return true;
    if (!user.assignedClientIds?.length) return true;
    return user.assignedClientIds.includes(clientId);
  };

  const canEditDocument = (clientId: string) => {
    if (isPlatformAdmin) return false;
    return canAccessClient(clientId);
  };

  return {
    isPlatformAdmin,
    isCompanyManager,
    isBackOfficeOperator,
    canManageUsers,
    canManageCompany,
    canAccessAllClients,
    canAccessClient,
    canEditDocument,
    canManageCompanies,
    canProvisionUsers,
    canViewMonitoring,
    canAccessCompanyData,
  };
}
