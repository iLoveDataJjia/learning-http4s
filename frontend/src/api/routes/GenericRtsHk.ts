import axios, { AxiosError } from "axios";
import { useMutation, useQuery } from "react-query";
import { useNavigate } from "react-router-dom";
import useAuth from "../../context/auth/AuthHk";
import { ToastLevelEnum } from "../../context/toaster/ToasterCtx";
import useToaster from "../../context/toaster/ToasterHk";
import { IDto } from "../dto/IDto";
import { BackendException, ODto, TokenODto } from "../dto/ODto";

/**
 * Server location.
 */
const SERVER_URL = `http://${window.location.hostname}:8081/`;

/**
 * Client side error toast.
 */
const CLIENT_ERR_TOAST = {
  level: ToastLevelEnum.Warning,
  header: "Unreachable server",
  message: "Please verify your internet connection.",
};

/**
 * Server side applicative error toast.
 */
const SERVER_APP_ERR_TOAST = (message: string) => ({
  level: ToastLevelEnum.Error,
  header: "Bad request",
  message: message,
});

/**
 * Server side authentication error toast.
 */
const SERVER_AUTH_ERR_TOAST = {
  level: ToastLevelEnum.Warning,
  header: "Not connected",
  message: "Connection lost or account not provided. Please reconnect.",
};

/**
 * Axios fetcher hook.
 */
function useApi(authed: boolean, verbose: boolean) {
  // Get hooks & Build backend api
  const navigate = useNavigate();
  const { addToast } = useToaster();
  const { tokens, setTokens } = useAuth();
  const api = axios.create({ baseURL: SERVER_URL });

  // Pre-request process
  api.interceptors.request.use(async (req) => {
    // Get fresh tokens if authed
    let freshTokens: TokenODto | undefined;
    if (authed && tokens && tokens.expireAt < Date.now()) {
      await axios
        .post<TokenODto>(`${SERVER_URL}user/refresh`, undefined, {
          headers: { "DataPiU-Refresh-Token": tokens.refreshToken },
        })
        .then((res) => {
          setTokens(res.data);
          freshTokens = res.data;
        })
        .catch((err: AxiosError<BackendException>) => {
          verbose && (err.response ? addToast(SERVER_AUTH_ERR_TOAST) : addToast(CLIENT_ERR_TOAST));
          freshTokens = undefined;
        });
    } else freshTokens = tokens;

    // Do request or abort (authed and no fresh tokens)
    const controller = new AbortController();
    if (authed) {
      if (freshTokens) req.headers.setAuthorization(`Bearer ${freshTokens.accessToken}`);
      else {
        controller.abort();
        navigate("/login");
      }
    }
    return { ...req, signal: controller.signal };
  });

  // Post-response process
  api.interceptors.response.use(
    (_) => _,
    (err: AxiosError<BackendException>) => {
      switch (err.response?.data.kind) {
        case "AppException":
          verbose && addToast(SERVER_APP_ERR_TOAST(err.response.data.message));
          break;
        case "AuthException":
          verbose && addToast(SERVER_AUTH_ERR_TOAST);
          navigate("/login");
          break;
        case undefined:
          verbose && addToast(CLIENT_ERR_TOAST);
          break;
      }
      return err;
    }
  );

  // Return
  return api;
}

/**
 * Get request hook.
 */
export function useGet<A extends ODto>(
  queryKey: string,
  subDirect: string,
  headers: object | undefined,
  authed: boolean,
  verbose: boolean,
  refetchInterval: number | false // In second(s)
) {
  // Hooks
  const api = useApi(authed, verbose);
  const { data, isLoading } = useQuery(
    queryKey,
    () => api.get<A>(subDirect, { headers: headers }).then((_) => _.data),
    { refetchInterval: refetchInterval ? refetchInterval * 1000 : refetchInterval }
  );

  // Return
  return { data, isLoading };
}

/**
 * Get request hook effect.
 */
export function useGetM<A extends ODto>(subDirect: string, authed: boolean, verbose: boolean) {
  // Hooks
  const api = useApi(authed, verbose);
  const {
    mutate: getMutate,
    data,
    isLoading,
  } = useMutation((headers: object | undefined) => api.get<A>(subDirect, { headers: headers }).then((_) => _.data));

  // Return
  const getM = (headers: object | undefined) => getMutate(headers);
  return { getM, data, isLoading };
}

/**
 * Post request hook effect.
 */
export function usePostM<A extends ODto>(subDirect: string, authed: boolean, verbose: boolean) {
  // Hooks
  const api = useApi(authed, verbose);
  const {
    mutate: postMutate,
    data,
    isLoading,
  } = useMutation((args: { body: IDto | undefined; headers: object | undefined }) =>
    api.post<A>(subDirect, args.body, { headers: args.headers }).then((_) => _.data)
  );

  // Return
  const postM = (body: IDto | undefined, headers: object | undefined) => postMutate({ body: body, headers: headers });
  return { postM, data, isLoading };
}
