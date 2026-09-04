import { expect, type APIRequestContext, type TestInfo } from '@playwright/test';
import { writeFile } from 'node:fs/promises';
import { assertPdf, json, mailpitURL, status } from './support';

interface MailSummary {
  ID: string;
  Subject: string;
  To: { Address: string }[];
}
interface MailMessage extends MailSummary {
  Text: string;
  HTML: string;
  Attachments: { FileName: string; ContentType: string; PartID: string }[];
}

// Official Mailpit v1 API: https://mailpit.axllent.org/docs/api-v1/ .
// Only reads: never release mail, follow links, send synthetic messages, or
// clear another run's mailbox. The frontdesk -> notification -> SMTP chain
// must produce the messages that we observe here.
export async function receivedMail(request: APIRequestContext, info: TestInfo,
  email: string, subject: string, expectedText: string[], invoiceId?: string): Promise<void> {
  expect(email.endsWith('.test')).toBe(true);
  let found: MailSummary | undefined;
  await expect.poll(async () => {
    const response = await request.get(`${mailpitURL}/api/v1/search`, {
      params: { query: `to:${email}`, limit: 50 }, timeout: 5_000, maxRedirects: 0,
    });
    const result = await json<{ messages: MailSummary[] }>(response);
    found = result.messages.find(message => message.Subject === subject
      && message.To.some(to => to.Address === email));
    return Boolean(found);
  }, {
    message: `Frontdesk notification must reach Mailpit: ${subject} -> ${email}`,
    timeout: 30_000, intervals: [250, 500, 1000, 2000],
  }).toBe(true);
  const message = await json<MailMessage>(await request.get(
    `${mailpitURL}/api/v1/message/${encodeURIComponent(found!.ID)}`, { timeout: 5_000, maxRedirects: 0 }));
  expect(message.Subject).toBe(subject);
  expect(message.To.map(to => to.Address)).toContain(email);
  for (const text of expectedText) expect(`${message.Text}\n${message.HTML}`).toContain(text);
  await info.attach(`mail-${subject}.json`, {
    body: JSON.stringify(message, null, 2), contentType: 'application/json',
  });
  if (invoiceId) {
    const pdf = message.Attachments.find(item => item.FileName === `factura-${invoiceId}.pdf`);
    expect(pdf, 'Checkout must include the real invoice PDF fetched from billing').toBeDefined();
    expect(pdf!.ContentType).toContain('application/pdf');
    const attachment = await request.get(
      `${mailpitURL}/api/v1/message/${encodeURIComponent(message.ID)}/part/${encodeURIComponent(pdf!.PartID)}`,
      { timeout: 10_000, maxRedirects: 0 });
    await status(attachment, 200);
    const bytes = await attachment.body();
    const pdfPath = info.outputPath('checkout-mail-invoice.pdf');
    await writeFile(pdfPath, bytes);
    await info.attach('checkout-mail-invoice.pdf', { path: pdfPath, contentType: 'application/pdf' });
    assertPdf(bytes);
  }
}
