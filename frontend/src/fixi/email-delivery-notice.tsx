/** Consistent guidance shown after an action sends an email through Fixi. */
export function EmailDeliveryNotice() {
  return (
    <p role="note" className="rounded-md bg-muted p-3 text-sm text-muted-foreground">
      La persona destinataria debe revisar su bandeja de entrada y, si no encuentra el correo,
      Spam o correo no deseado.
    </p>
  )
}
