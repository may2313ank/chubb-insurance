import http from 'k6/http';
import { check, group } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const POLICIES_PATH = '/api/v1/policies';
const SUMMARY_PATH = '/api/v1/policies/summary';

const failureRate = new Rate('failed_requests');
const listDuration = new Trend('list_duration', true);
const summaryDuration = new Trend('summary_duration', true);

const STATUS_VALUES = ['ACTIVE', 'PENDING', 'EXPIRED', 'CANCELLED'];
const LINE_OF_BUSINESS_VALUES = ['PROPERTY', 'CASUALTY', 'ACCIDENT_AND_HEALTH', 'MARINE'];
const REGION_VALUES = [
  'Singapore', 'Hong Kong', 'Australia', 'Japan',
  'Thailand', 'Indonesia', 'Malaysia', 'Philippines',
];

export const options = {
  scenarios: {
    steady_read_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 20 },
        { duration: '1m', target: 50 },
        { duration: '30s', target: 0 },
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    failed_requests: ['rate<0.01'],
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
    list_duration: ['p(95)<500'],
    summary_duration: ['p(95)<300'],
  },
};

function pick(values) {
  return values[Math.floor(Math.random() * values.length)];
}

function buildListParams() {
  const params = { size: pick([10, 20, 50]), page: pick([0, 0, 0, 1]) };
  if (Math.random() < 0.6) {
    params.status = pick(STATUS_VALUES);
  }
  if (Math.random() < 0.4) {
    params.lineOfBusiness = pick(LINE_OF_BUSINESS_VALUES);
  }
  if (Math.random() < 0.3) {
    params.region = pick(REGION_VALUES);
  }
  return params;
}

function toQueryString(params) {
  return Object.keys(params)
    .map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&');
}

export default function () {
  group('list policies', () => {
    const url = `${BASE_URL}${POLICIES_PATH}?${toQueryString(buildListParams())}`;
    const res = http.get(url, { tags: { endpoint: 'list_policies' } });
    listDuration.add(res.timings.duration);
    const ok = check(res, {
      'list status is 200': (r) => r.status === 200,
      'list has content array': (r) => {
        try {
          return Array.isArray(r.json('content'));
        } catch (e) {
          return false;
        }
      },
    });
    failureRate.add(!ok);
  });

  group('policy summary', () => {
    const res = http.get(`${BASE_URL}${SUMMARY_PATH}`, { tags: { endpoint: 'policy_summary' } });
    summaryDuration.add(res.timings.duration);
    const ok = check(res, {
      'summary status is 200': (r) => r.status === 200,
    });
    failureRate.add(!ok);
  });
}