# Parallax Onboarding (Jetpack Compose)

A visually rich onboarding experience built with Jetpack Compose.  
This project demonstrates layered parallax animations, smooth transitions, and dynamic color
interpolation between onboarding pages.

---

## Preview

![Onboarding Preview](Onboarding_Parallax_Preview.gif)

---

## Overview

This onboarding screen is built using `HorizontalPager` and multiple animated layers that respond to
scroll offset. Each page has its own visual identity defined by colors, shapes, and background
elements.

The main idea is to create depth and motion by combining multiple parallax layers moving at
different speeds.

---

## Key Features

- Horizontal pager-based onboarding flow
- Multi-layer parallax animation system
- Animated background blobs and geometric shapes
- Dynamic color interpolation between pages
- Smooth indicator animation (width + color transition)
- Adaptive button styling based on scroll position
- Lightweight Canvas-based rendering for performance

---

## Animation Concept

Each page consists of several layers:

- Background gradient blobs with slow parallax movement
- Floating geometric shapes with medium-depth motion
- Central glow element with subtle scaling and fading
- Text content that reacts to scroll position

All animations are driven by pager offset, creating a continuous and smooth transition between
pages.

---

## Data Model

Each onboarding page is defined by:

- Title and subtitle text
- Background color
- Accent and shape colors
- List of blobs (position + size)
- List of geometric shapes

This makes the onboarding fully data-driven and easy to extend.

---

## How to Run

Simply include the onboarding screen in your Compose UI:

OnboardingScreen()

---

## Notes

This implementation focuses on:

- motion as a storytelling tool
- smooth transitions instead of abrupt changes
- lightweight performance-friendly rendering using Canvas
- expressive UI with minimal dependencies

---