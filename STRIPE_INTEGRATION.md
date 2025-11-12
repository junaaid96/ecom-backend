# Stripe Payment Gateway Integration

This document describes the Stripe payment gateway integration for the e-commerce backend.

## Overview

The application integrates with Stripe to process payments securely. The integration supports:
- Creating payment intents for orders
- Processing payments with various payment methods
- Webhook handling for payment status updates
- Automatic order status updates based on payment events

## Configuration

### Environment Variables

Set the following environment variables:

```bash
STRIPE_API_KEY=sk_test_your_stripe_secret_key_here
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret_here
```

Or update the values in `application.yml`:

```yaml
stripe:
  api-key: ${STRIPE_API_KEY:sk_test_your_stripe_secret_key_here}
  webhook-secret: ${STRIPE_WEBHOOK_SECRET:whsec_your_webhook_secret_here}
  currency: usd
```

### Getting Stripe API Keys

1. Create a Stripe account at https://stripe.com
2. Navigate to Developers > API keys
3. Copy your Secret key (starts with `sk_test_` for test mode)
4. Create a webhook endpoint and copy the webhook signing secret (starts with `whsec_`)

## API Endpoints

### 1. Create Payment Intent

**Endpoint:** `POST /api/payments/create-intent`

**Authentication:** Required (JWT token)

**Request Body:**
```json
{
  "orderId": 1
}
```

**Response:**
```json
{
  "paymentId": 1,
  "clientSecret": "pi_xxx_secret_xxx",
  "paymentIntentId": "pi_xxx",
  "amount": 99.99,
  "currency": "usd",
  "status": "requires_payment_method"
}
```

**Description:** Creates a Stripe payment intent for the specified order. Returns a client secret that should be used on the frontend with Stripe.js to collect payment details.

### 2. Get Payment by Order ID

**Endpoint:** `GET /api/payments/order/{orderId}`

**Authentication:** Required (JWT token)

**Response:**
```json
{
  "id": 1,
  "orderId": 1,
  "stripePaymentIntentId": "pi_xxx",
  "amount": 99.99,
  "currency": "usd",
  "status": "SUCCEEDED",
  "paymentMethodId": "pm_xxx",
  "receiptUrl": null,
  "failureMessage": null,
  "createdAt": "2025-11-12T10:00:00Z",
  "updatedAt": "2025-11-12T10:05:00Z"
}
```

### 3. Get Payment by ID (Admin Only)

**Endpoint:** `GET /api/payments/{id}`

**Authentication:** Required (JWT token with ADMIN role)

**Response:** Same as above

### 4. Stripe Webhook

**Endpoint:** `POST /api/payments/webhook`

**Authentication:** None (Public, verified via Stripe signature)

**Headers:**
- `Stripe-Signature`: Webhook signature from Stripe

**Description:** Handles webhook events from Stripe. Supported events:
- `payment_intent.succeeded`: Marks payment as succeeded and updates order status to PROCESSING
- `payment_intent.payment_failed`: Marks payment as failed with error message
- `payment_intent.canceled`: Marks payment as cancelled

## Database Schema

### Payment Entity

```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id),
    stripe_payment_intent_id VARCHAR(255) NOT NULL UNIQUE,
    stripe_client_secret VARCHAR(500),
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_method_id VARCHAR(255),
    receipt_url VARCHAR(500),
    failure_message VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

### Payment Status Enum

- `PENDING`: Payment intent created, awaiting payment
- `PROCESSING`: Payment is being processed
- `SUCCEEDED`: Payment completed successfully
- `FAILED`: Payment failed
- `CANCELLED`: Payment was cancelled
- `REFUNDED`: Payment was refunded

## Frontend Integration

### Example: Complete Payment Flow

```javascript
// 1. Place an order
const orderResponse = await fetch('/api/orders', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    shippingAddress: '123 Main St, City, State 12345',
    items: [
      { productId: 1, quantity: 2 }
    ]
  })
});
const order = await orderResponse.json();

// 2. Create payment intent
const paymentResponse = await fetch('/api/payments/create-intent', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    orderId: order.id
  })
});
const payment = await paymentResponse.json();

// 3. Use Stripe.js to collect payment details
const stripe = Stripe('pk_test_your_publishable_key');
const elements = stripe.elements({ clientSecret: payment.clientSecret });
const paymentElement = elements.create('payment');
paymentElement.mount('#payment-element');

// 4. Submit payment
const { error } = await stripe.confirmPayment({
  elements,
  confirmParams: {
    return_url: 'http://localhost:3000/order-confirmation'
  }
});

if (error) {
  console.error('Payment failed:', error.message);
}
```

## Webhook Setup in Stripe Dashboard

1. Go to Stripe Dashboard > Developers > Webhooks
2. Click "Add endpoint"
3. Enter your endpoint URL: `https://yourdomain.com/api/payments/webhook`
4. Select events to listen to:
   - `payment_intent.succeeded`
   - `payment_intent.payment_failed`
   - `payment_intent.canceled`
5. Copy the webhook signing secret and add it to your environment variables

## Testing

### Test Mode

Use Stripe test mode for development:
- API keys starting with `sk_test_` and `pk_test_`
- Test card numbers: https://stripe.com/docs/testing

Example test cards:
- Success: `4242 4242 4242 4242`
- Declined: `4000 0000 0000 0002`
- Requires authentication: `4000 0025 0000 3155`

### Testing Webhooks Locally

Use Stripe CLI to forward webhooks to your local server:

```bash
# Install Stripe CLI
# https://stripe.com/docs/stripe-cli

# Login to Stripe
stripe login

# Forward webhooks to local endpoint
stripe listen --forward-to localhost:8080/api/payments/webhook

# Copy the webhook signing secret and add to your environment
```

## Security Considerations

1. **Never expose API keys**: Always use environment variables for sensitive credentials
2. **Verify webhook signatures**: The webhook endpoint verifies Stripe signatures to prevent spoofing
3. **Use HTTPS in production**: Stripe requires HTTPS for webhook endpoints in live mode
4. **Validate amounts**: Always validate payment amounts match order totals on the backend
5. **Handle idempotency**: Payment operations are idempotent - creating a payment intent for an order that already has one will fail

## Error Handling

The integration handles various error scenarios:
- Order not found
- Payment already exists for order
- Stripe API errors
- Invalid webhook signatures
- Payment failures

All errors are logged and appropriate HTTP status codes are returned.

## Order Status Flow

1. Order placed → `PENDING`
2. Payment intent created → Order remains `PENDING`
3. Payment succeeds → Order updated to `PROCESSING`
4. Payment fails → Order remains `PENDING`

## Additional Resources

- [Stripe API Documentation](https://stripe.com/docs/api)
- [Stripe Java Library](https://stripe.com/docs/api/java)
- [Stripe Testing Guide](https://stripe.com/docs/testing)
- [Stripe Webhooks](https://stripe.com/docs/webhooks)
