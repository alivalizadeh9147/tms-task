import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 500,
    iterations: 500,
};

const accountId = '8f67c2e3-f3da-468f-9772-4036b4878d47';

export default function () {

    const idempotencyKey =
        `concurrent-debit-${__VU}-${__ITER}`;

    const response = http.post(
        `http://host.docker.internal:8080/accounts/${accountId}/debit`,

        JSON.stringify({
            amount: 100
        }),

        {
            headers: {
                'Content-Type': 'application/json',
                'Idempotency-Key': idempotencyKey,
            },
            responseCallback: http.expectedStatuses(200, 409),
        }
    );

    check(response, {
        'status is 200 or 409': (r) =>
            r.status === 200 || r.status === 409,
    });
}