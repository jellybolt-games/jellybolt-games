# Link — Project Knowledge

## Project Context
JellyBolt Games — Free mobile games studio for kids & teens.
Stack: React Native, Expo, TypeScript, Supabase
Owner: tdsquadAI Team

## Backend Stack
- **Platform:** Supabase
- **Database:** PostgreSQL
- **Auth:** Supabase Auth (anonymous + email)
- **Realtime:** Supabase Realtime (multiplayer)
- **Functions:** Supabase Edge Functions (Deno)
- **Billing:** RevenueCat webhooks
- **Analytics:** Firebase Analytics

## Key Decisions
- Supabase free tier for MVP
- Row-level security (RLS) on all tables
- Anonymous auth for frictionless onboarding

## Learnings

### Backend & Infrastructure (seeded 2026-03-22)
- **Supabase setup:** PostgreSQL backend, anonymous + email auth, Realtime for multiplayer features, Edge Functions (Deno)
- **Current status:** Browser games don't require backend; Supabase will be critical when React Native mobile apps launch
- **Billing integration:** RevenueCat webhooks configured for Google Play Billing (15% commission on first $1M/year)
- **Analytics:** Firebase Analytics + Remote Config for A/B testing prices and features
- **Privacy compliance:** COPPA-aware, minimal data collection, privacy policy in place
- **Multiplayer readiness:** Supabase Realtime enables features like Quiz Battle leaderboards
- **Key decisions made:** Supabase free tier for MVP, row-level security (RLS) on all tables, anonymous auth for frictionless onboarding
- **Monetization backend:** RevenueCat free tier until $2,500/month tracked revenue, then 1% commission

