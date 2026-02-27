---
name: "web-design-guidelines"
description: "Review UI code for compliance with web interface best practices. Use when user asks to 'Review my UI', 'Check accessibility', 'Audit design', 'Review UX', or 'Check my site against best practices'. Audits code for 100+ rules covering accessibility, performance, and UX."
---

# Web Design Guidelines

Review UI code for compliance with web interface best practices. This skill audits your code for 100+ rules covering accessibility, performance, and UX.

## When to Use

- "Review my UI"
- "Check accessibility"
- "Audit design"
- "Review UX"
- "Check my site against best practices"

## Categories Covered

### Accessibility
- Use proper `aria-labels` for interactive elements
- Use semantic HTML elements (`<button>`, `<nav>`, `<main>`, `<article>`, etc.)
- Implement keyboard handlers for all interactive elements
- Ensure sufficient color contrast (WCAG 2.1 AA minimum)
- Provide text alternatives for non-text content
- Use `role` attributes appropriately
- Ensure focus order is logical

### Focus States
- Always provide visible focus indicators
- Use `:focus-visible` for keyboard-only focus styles
- Avoid removing focus outlines without replacement
- Ensure focus states have sufficient contrast

### Forms
- Use proper `autocomplete` attributes
- Implement client-side validation with clear error messages
- Associate `<label>` elements with form controls
- Use appropriate `input` types (`email`, `tel`, `number`, etc.)
- Provide clear error handling and recovery instructions
- Use `aria-describedby` for form hints and errors

### Animation
- Respect `prefers-reduced-motion` media query
- Use compositor-friendly transforms (`transform`, `opacity`)
- Avoid animating layout properties (`width`, `height`, `top`, `left`)
- Keep animations under 200ms for UI interactions
- Provide pause/stop controls for auto-playing content

### Typography
- Use proper curly quotes (`"..."` not `"..."`)
- Use CSS `text-overflow: ellipsis` for truncated text
- Use `font-variant-numeric: tabular-nums` for numbers in tables
- Ensure readable line height (1.5 minimum for body text)
- Use appropriate font sizes (16px minimum for body text)

### Images
- Always include `width` and `height` attributes to prevent layout shift
- Implement lazy loading for below-fold images (`loading="lazy"`)
- Provide descriptive `alt` text for meaningful images
- Use empty `alt=""` for decorative images
- Use modern formats (WebP, AVIF) with fallbacks
- Implement responsive images with `srcset` and `sizes`

### Performance
- Virtualize long lists to reduce DOM size
- Avoid layout thrashing (batch DOM reads/writes)
- Use `preconnect` for critical third-party origins
- Implement code splitting for JavaScript
- Use CSS containment (`contain`) for complex components
- Minimize main thread work

### Navigation & State
- URL should reflect application state
- Implement deep-linking for key views
- Use `history.pushState` for client-side navigation
- Provide breadcrumb navigation for deep hierarchies
- Ensure back button works correctly

### Dark Mode & Theming
- Use `color-scheme` meta tag
- Implement `theme-color` meta tag for mobile browsers
- Use CSS custom properties for theme values
- Respect `prefers-color-scheme` media query
- Ensure all colors adapt properly in dark mode

### Touch & Interaction
- Use `touch-action` to control gesture handling
- Disable `tap-highlight-color` when custom feedback is provided
- Ensure touch targets are at least 44x44 pixels
- Provide visual feedback for touch interactions
- Avoid hover-only interactions on touch devices

### Locale & i18n
- Use `Intl.DateTimeFormat` for date formatting
- Use `Intl.NumberFormat` for number/currency formatting
- Support RTL layouts with `dir="rtl"`
- Use Unicode-aware string operations
- Handle pluralization correctly with `Intl.PluralRules`

## Review Process

When reviewing UI code:

1. **Accessibility First**: Check for keyboard navigation, screen reader support, and color contrast
2. **Performance Check**: Look for layout thrashing, missing lazy loading, and bundle size issues
3. **UX Audit**: Verify focus states, form validation, and responsive behavior
4. **Best Practices**: Ensure semantic HTML, proper ARIA usage, and modern CSS patterns

## Output Format

Provide a structured review with:

```
## Accessibility Issues
- [CRITICAL] Missing aria-label on button
- [WARNING] Color contrast ratio 3.2:1 (needs 4.5:1)

## Performance Issues
- [HIGH] Large image without lazy loading
- [MEDIUM] Layout thrashing in scroll handler

## UX Issues
- [MEDIUM] Focus state not visible
- [LOW] Touch target too small (32x32)

## Recommendations
1. Add aria-label="Close menu" to the close button
2. Increase button padding to meet 44x44 minimum
3. Add loading="lazy" to below-fold images
```
