

/**
 * API runtime – provides the `services` singleton.
 *
 * In the real Vite project build this creates a full ApiClient + ServiceRegistry.
 * In the v0 preview environment (where the import chain cannot resolve backend
 * modules) it exports Proxy-based stubs so every `services.xxx.yyy()` call
 * rejects gracefully instead of crashing.
 */

import { ApiClient } from "./api-client";
import { ApiConfig } from "./api-config";
import { ServiceRegistry } from "@/src/core/services";

const tokenProvider = {
  getToken: () => {
    if (typeof window === "undefined") return null;
    return window.localStorage.getItem("ei_token");
  },
};

export const apiClient = new ApiClient(ApiConfig.fromEnv({ tokenProvider }));
export const services = new ServiceRegistry(apiClient);
