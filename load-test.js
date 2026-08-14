import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 500,
    duration: '30s',
};

const accounts = [
    'd0bb3c41-9d85-48d4-a9cf-e819c3659d86',
    '0cea8d6b-488a-41c7-8488-95e4346d549a',
    'd6ef1ee4-75f6-4dc7-a706-76ac0f89a9ce',
    '8f67c2e3-f3da-468f-9772-4036b4878d47',
    'f572c5b5-a564-4888-b1c0-5f8b599d0145',
    '418c70c5-5f6f-473b-9043-fd7c70da9c53',
    'f0aabad6-7966-485e-9b60-a602814b6649',
    '772e9512-60a1-4006-949b-cf3d606d5889',
    '2493b95c-f37d-4fb8-b819-5016202b1960',
    'b90f5ccc-f1ac-487d-8095-c88238657542',
];

const accountId =
    accounts[Math.floor(Math.random() * accounts.length)];

export default function () {

    const response = http.post(
        `http://host.docker.internal:8080/accounts/${accountId}/credit`,

        JSON.stringify({
            amount: 100
        }),

        {
            headers: {
                'Content-Type': 'application/json',
                'Idempotency-Key': `${__VU}-${__ITER}`,
            },
        }
    );

    check(response, {
        'status is 200': (r) => r.status === 200,
    });
}