import { HttpClient, HttpRequest, HttpResponse } from "./http-types";

/** HttpClient implementation using the browser Fetch API. */
export class FetchHttpClient implements HttpClient {
  /**
   * Sends a request using `fetch` and parses the response.
   * @param request request configuration
   */
  async send<TResponse, TBody = unknown>(request: HttpRequest<TBody>): Promise<HttpResponse<TResponse>> {
    const timeoutMs = Number(import.meta.env.VITE_API_TIMEOUT_MS ?? 15000);
    const timeoutController = new AbortController();
    const timeoutId = window.setTimeout(() => timeoutController.abort(), timeoutMs);

    const mergedSignal = request.signal
      ? AbortSignal.any([request.signal, timeoutController.signal])
      : timeoutController.signal;

    const init: RequestInit = {
      method: request.method,
      headers: request.headers,
      signal: mergedSignal,
    };

    if (request.body !== undefined) {
      if (request.body instanceof FormData) {
        init.body = request.body;
      } else {
        init.body = JSON.stringify(request.body);
      }
    }

    try {
      const response = await fetch(request.url, init);
      const responseType = request.responseType ?? "json";

      let data: TResponse;
      if (response.status === 204) {
        data = null as TResponse;
      } else if (responseType === "text") {
        data = (await response.text()) as TResponse;
      } else if (responseType === "blob") {
        data = (await response.blob()) as TResponse;
      } else if (responseType === "arrayBuffer") {
        data = (await response.arrayBuffer()) as TResponse;
      } else {
        const contentType = response.headers.get("content-type") ?? "";
        if (contentType.includes("application/json")) {
          data = (await response.json()) as TResponse;
        } else {
          data = (await response.text()) as TResponse;
        }
      }

      return {
        ok: response.ok,
        status: response.status,
        headers: response.headers,
        data,
      };
    } finally {
      window.clearTimeout(timeoutId);
    }
  }
}
