# StreamHub frontend

React/Vite interface for the StreamHub API: catalogue search, genre filters, pagination, personal demo sessions, sign in, favourites, video previews and saved watch progress.

## Local development

Use Node.js 22.12+.

```sh
npm ci
cp .env.example .env.local
npm run dev
```

Set `VITE_API_BASE_URL` to the public StreamHub API URL. It defaults to `http://localhost:8080`; `/api` supports a same-origin reverse proxy, and an empty value uses the current origin. Allow the frontend origin in the API's CORS configuration.

## Vercel

- Root directory: `frontend`
- Preset: Vite
- Build: `npm run build`
- Output: `dist`
- Environment: `VITE_API_BASE_URL=https://<your-streamhub-api>`

Redeploy after changing this build-time environment variable. Free backend services can sleep between visits: the UI explains prolonged startup after six seconds, allows requests up to 150 seconds, and shows a retry action after failure.

## Verification

```sh
npm run lint
npm test
npm run build
```

The Vitest tests use API fixtures and cover search/pagination, saved movies outside the current catalogue page, favourites, progress, registration/sign in/sign out and connection errors. They do not prove the deployed API is available.

Before sharing the URL, test against the deployed API: catalogue → demo session → add favourite → save progress → inspect Favourites and Continue watching → sign out. Verify registration and sign in separately.
