# External Integrations

## Overview

This package contains the application's integrations with external systems.

According to the project requirements, HackHub interacts with:

* A **Calendar** service used to schedule mentor-team calls.
* A **Payment** service used to transfer the prize to the winning team.

To keep the application independent from specific third-party providers, all integrations are accessed through dedicated interfaces.

---

## Fake Implementations

The current implementation uses **fake clients** instead of real external services.

These fake clients simulate the behavior of external systems by generating mock identifiers and returning successful responses without performing any actual network communication.

Examples include:

* `FakeCalendarClient`
* `FakePaymentClient`

This approach allows the application to be developed, tested, and demonstrated without requiring access to real third-party APIs.

---

## Benefits

Using fake integrations provides several advantages:

* No external accounts or API keys are required.
* Development and testing can be performed offline.
* Application behavior remains deterministic and predictable.
* Real implementations can be introduced later without changing the business logic.

---

## Future Evolution

If real providers are adopted in the future, new implementations can be created for the existing interfaces while leaving the application services unchanged.

For example:

```text
CalendarClient
 ├── FakeCalendarClient
 └── GoogleCalendarClient

PaymentClient
 ├── FakePaymentClient
 └── StripePaymentClient
```

This follows the Dependency Inversion Principle, allowing the domain and application layers to depend on abstractions rather than concrete external services.
