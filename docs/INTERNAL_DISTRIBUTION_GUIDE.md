# Internal Distribution Guide

## Play Console Internal Testing Track Setup

### Prerequisites
1. Google Play Console account with app `com.touchstoneinstitute.learningcompanion` registered
2. Service account with Play Developer API access (see `ENVIRONMENT_SECRETS_INVENTORY.md`)
3. Signed release AAB (produced by CI on main branch push)

### Setup Steps

1. **Create Internal Testing Track**
   - Go to Play Console → TSIN Learning Companion → Testing → Internal testing
   - Click "Create new release"
   - Upload the signed AAB (or let CI handle this via `r0adkll/upload-google-play`)

2. **Configure Testers**
   - Go to Internal testing → Testers tab
   - Create a new email list: "TSIN QA Team"
   - Add internal QA team member emails
   - Copy the opt-in link and share with testers

3. **Tester Onboarding**
   - Each tester must accept the invite link
   - Install from Play Store (internal track)
   - App updates will be pushed automatically when CI deploys

### CI/CD Integration

The `android.yml` workflow automatically:
1. Builds a signed AAB on `main` branch push
2. Uploads to the Play Store internal testing track
3. Internal testers receive the update within ~10 minutes

### Manual Upload (Fallback)

If CI upload fails:
1. Download the `release-aab` artifact from the GitHub Actions run
2. Go to Play Console → Internal testing → Create new release
3. Upload the AAB manually
4. Add release notes and roll out

### Promoting to Production

1. Go to Internal testing → Release dashboard
2. Click "Promote release" → Production
3. Set rollout percentage (start with 10%, then 50%, then 100%)
4. Fill in release notes for public listing
5. Submit for review

