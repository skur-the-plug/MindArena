# Demo Credentials

These accounts are seeded by the optional legacy UI demo-data initializer. Enable
it only for local demonstrations:

```bash
export DEMO_DATA_ENABLED=true
export DEMO_USER_PASSWORD=choose-a-local-demo-password
```

| Level | Name | Email | Password |
| --- | --- | --- | --- |
| Champion | Nadia Bennani | `nadia@mindarena.local` | Value of `DEMO_USER_PASSWORD` |
| Expert | Omar Haddad | `omar@mindarena.local` | Value of `DEMO_USER_PASSWORD` |
| Challenger | Leila Mansouri | `leila@mindarena.local` | Value of `DEMO_USER_PASSWORD` |
| Explorer | Youssef El Amrani | `youssef@mindarena.local` | Value of `DEMO_USER_PASSWORD` |
| Rookie | Sara Idrissi | `sara@mindarena.local` | Value of `DEMO_USER_PASSWORD` |

If demo admin seeding is enabled with `DEMO_ADMIN_ENABLED=true`, the admin email
and password come from `DEMO_ADMIN_EMAIL` and `DEMO_ADMIN_PASSWORD`.

Demo credentials are deliberately absent from `.env.example`. Supply local
values in the ignored `.env` file and never reuse them outside development.
