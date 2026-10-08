import { test, expect } from '@playwright/test';
import { randomUUID } from 'node:crypto';
import { json, PmsApi, primaryCredentials, baseURL, uniqueTag } from './support';

test('creates and transitions a tenant-scoped FIXI workflow', async ({ browser }) => {
  test.setTimeout(120_000);
  const context = await browser.newContext({ baseURL });
  try {
    const api = new PmsApi(await context.newPage());
    const user = await api.login(primaryCredentials());
    const workflow = await json<{ id: string; name: string }>(await api.mutate('POST', '/api/v1/workflows', {
      verticalKey: uniqueTag(), name: `${uniqueTag()} repair lifecycle`,
    }), 201);
    await json(await api.mutate('POST', `/api/v1/workflows/${workflow.id}/states`, {
      key: 'intake', label: 'Intake', semanticPhase: 'INTAKE', initialState: true, terminalState: false,
    }), 200);
    await json(await api.mutate('POST', `/api/v1/workflows/${workflow.id}/states`, {
      key: 'diagnosis', label: 'Diagnosis', semanticPhase: 'DIAGNOSIS', initialState: false, terminalState: false,
    }), 200);
    await json(await api.mutate('POST', `/api/v1/workflows/${workflow.id}/transitions`, {
      fromStateKey: 'intake', toStateKey: 'diagnosis', transitionKey: 'diagnose', requiredPermission: 'workflow.transition',
    }), 200);
    const aggregateId = randomUUID();
    const transitioned = await json<{ key: string; fromStateKey: string; toStateKey: string }>(
      await api.mutate('POST', `/api/v1/workflows/${workflow.id}/transition`, {
        aggregateId, fromStateKey: 'intake', transitionKey: 'diagnose',
      }), 200);
    expect(user.hotelId).toBeTruthy();
    expect(transitioned).toMatchObject({ key: 'diagnose', fromStateKey: 'intake', toStateKey: 'diagnosis' });
  } finally {
    await context.close();
  }
});
