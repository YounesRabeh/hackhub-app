/**
 * Payment integration adapter layer.
 *
 * <p>Strategy Pattern: business services depend on {@code PaymentClient}
 * and can switch implementations (fake or real gateway) without touching core logic.</p>
 */
package com.hackhub.infrastructure.external.payment;
