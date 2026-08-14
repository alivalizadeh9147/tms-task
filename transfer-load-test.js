import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 500,
    iterations: 500,
};

const sourceAccountId = 'f572c5b5-a564-4888-b1c0-5f8b599d0145';
const destinationAccountId = '8f67c2e3-f3da-468f-9772-4036b4878d47';

export default function () {

    const idempotencyKey =
        `transfer-load-test-${__VU}-${__ITER}`;

    const response = http.post(
        `http://host.docker.internal:8080/accounts/${sourceAccountId}/transfer`,

        JSON.stringify({
            destinationAccountId: destinationAccountId,
            amount: 100,
            transactionId: idempotencyKey
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