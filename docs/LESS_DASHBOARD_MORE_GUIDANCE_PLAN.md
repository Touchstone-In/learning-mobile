# TSIN Learning Mobile: “Less Dashboard, More Guidance” Implementation Plan

## Purpose

Shift the app from a console-like, report-heavy experience toward a calmer, learner-guided mobile product that helps users quickly understand what matters now, what to do next, and how to recover when data is missing or delayed.

## North Star Outcome

The app should feel:

- supportive rather than transactional
- glanceable rather than text-dense
- action-oriented rather than report-oriented
- trustworthy in loading, empty, error, and offline states
- premium through spacing, hierarchy, and consistency

## Core UX Principles

1. Lead with the next best action, not raw status text.
2. Make the most important information visually dominant.
3. Replace generic states with guided states.
4. Reduce card clutter; use surface changes intentionally.
5. Use brand accents sparingly for emphasis, not decoration.
6. Prefer mobile-native patterns over dashboard/admin patterns.
7. Improve clarity before adding features.

## Delivery Structure

The work should be delivered in six phases, with each phase producing a visible UX improvement and a stable checkpoint for validation.

## Phase 0 — Baseline, Audit, and Success Criteria

### Goal
Create a measurable baseline before UI refactoring begins.

### Work
- Capture current screenshots for Login, Home, Schedule, Notifications, and Settings.
- Record current loading, empty, and error states for each main screen.
- Audit typography sizes, spacing patterns, and tap target sizes.
- Review contrast for navy, lime, and neutral surfaces in light and dark themes.
- Define success metrics for readability, task completion, and perceived polish.

### Deliverables
- screenshot baseline set
- UX issue list grouped by severity
- success metric checklist

### Exit Criteria
- team agrees on top usability pain points
- baseline visuals are saved for before/after comparison

## Phase 1 — Foundation System Refresh

### Goal
Build a reusable visual and interaction system that supports guided UX across the app.

### Work
- Rebalance typography to feel more mobile and less console-dense.
- Standardize spacing rhythm around 4, 8, 12, 16, 24, and 32 dp.
- Define shared section patterns: hero block, summary row, supporting text, action row.
- Create reusable UI states: loading, empty, error, and offline containers.
- Introduce shared components for chips, status labels, and supportive banners.
- Review surface/elevation usage so cards are used intentionally, not everywhere.

### Deliverables
- updated typography scale
- spacing guidelines
- shared state components
- reusable section and status components

### Exit Criteria
- at least one screen can be rebuilt using shared UI primitives only
- loading/empty/error patterns are consistent across screens

## Phase 2 — Home Screen: Guidance-First Redesign

### Goal
Turn Home into a learner dashboard that answers “What matters now?” immediately.

### Work
- Make the next session the primary hero area when a session exists.
- Surface a clear status label such as Today, Tomorrow, Upcoming, or Action Needed.
- Convert progress from plain text into a visual progress indicator.
- Add one clear CTA, such as View schedule or Continue preparation.
- Reduce equal-weight card stacking; introduce a stronger visual hierarchy.
- Replace generic full-screen loading with skeleton or staged content placeholders.
- Upgrade error treatment to include a retry action and calm explanation.

### Deliverables
- redesigned Home layout
- visual progress component
- hero next-session block
- better state handling

### Exit Criteria
- a learner can identify their next session and next action within 3 seconds
- Home feels meaningfully different from a text report

## Phase 3 — Schedule Screen: Scanability and Confidence

### Goal
Make the schedule easy to scan, reassuring, and useful in motion.

### Work
- Highlight Today and Next Session visually.
- Improve each session row with clearer separation of title, time, and location.
- Add small metadata cues such as chips or icons where useful.
- Separate no-sessions, load-failure, and offline-with-cached-data states.
- Support pull-to-refresh or a more native refresh pattern.
- Consider future-ready hooks for add-to-calendar or map/open-location actions.

### Deliverables
- improved day grouping
- clearer session row component
- distinct state views for empty/error/offline

### Exit Criteria
- schedule content is scannable without reading every line
- users can identify the next relevant session with minimal effort

## Phase 4 — Notifications and Settings: Product Maturity

### Goal
Eliminate dead-end screens and make secondary screens feel intentional.

### Notifications Work
- Replace the placeholder empty screen with a real informational empty state.
- Explain what appears here and link to notification preferences.
- Design for unread/read states, timestamps, and future actionable items.
- Add badge support in navigation when unread items exist.

### Settings Work
- Shift from heavy card blocks to cleaner grouped settings rows.
- Make entire rows tappable, not just switches.
- Separate destructive actions into a clear danger zone.
- Add support rows such as Help, Privacy, Terms, and App Version details.
- Improve feedback when preferences are saved or fail to save.

### Deliverables
- mature notifications empty state
- upgraded settings information architecture
- reusable settings row component

### Exit Criteria
- no primary-nav screen feels like a placeholder
- Settings feels native, simple, and trustworthy

## Phase 5 — Navigation, Motion, and Brand Polish

### Goal
Refine the shell so the app feels cohesive, calm, and premium.

### Work
- Unify screen naming, especially Notifications vs Alerts.
- Refine top app bar behavior per screen context.
- Ensure bottom navigation supports badges and selected-state clarity.
- Apply subtle motion for state transitions and content loading.
- Use brand lime as a focused accent, not a recurring structural color.
- Review dark theme parity and ensure the brand system remains readable.

### Deliverables
- updated app shell behavior
- naming consistency
- motion guidelines for key transitions

### Exit Criteria
- navigation feels consistent and intentional across all screens
- motion supports clarity without adding distraction

## Phase 6 — Accessibility, Validation, and Rollout

### Goal
Confirm the redesign improves usability, not just aesthetics.

### Work
- Validate 48dp minimum tap targets on interactive elements.
- test large font scaling and layout resilience
- review TalkBack labels and state announcements
- compare before/after screenshots side by side
- run targeted usability reviews on first-time and returning-user flows
- confirm no regressions in performance or navigation behavior

### Deliverables
- accessibility checklist
- before/after visual comparison
- UX validation summary

### Exit Criteria
- core flows remain stable
- accessibility issues are addressed or logged for follow-up
- redesign is ready for staged rollout

## Recommended Implementation Order Inside the Codebase

1. Update typography and shared spacing.
2. Build shared state components.
3. Redesign Home using the new system.
4. Redesign Schedule using the same primitives.
5. Upgrade Settings structure and row behavior.
6. Replace Notifications placeholder patterns.
7. Refine AppShell naming, badges, and bar behavior.
8. Finish accessibility and motion polish.

## Suggested Success Metrics

- users can identify their next session faster
- fewer screens rely on plain text-only status presentation
- consistent loading, empty, and error states across primary screens
- improved readability at common font scales
- stronger visual distinction between primary and secondary information
- higher perceived polish in before/after review

## Risks and Mitigations

- Risk: visual inconsistency during transition phases  
  Mitigation: land shared components first, then migrate screens in order.
- Risk: over-branding or excessive accent use  
  Mitigation: reserve lime for emphasis, status, and brand moments only.
- Risk: polishing visuals without improving usability  
  Mitigation: require every redesign to improve task clarity and state handling.
- Risk: regression in accessibility  
  Mitigation: validate tap targets, contrast, and screen reader labels in each phase.

## Immediate Next Sprint Recommendation

Focus the first sprint on the highest-leverage work:

- Phase 1 foundation system refresh
- Phase 2 Home redesign
- shared loading/empty/error components

This creates the design language needed for the rest of the product and delivers the most visible quality improvement early.