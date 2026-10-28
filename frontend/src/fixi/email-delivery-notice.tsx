/** Consistent guidance shown after an action sends an email through Fixi. */
export function EmailDeliveryNotice() {
  return (
    <p role="note" className="rounded-md bg-muted p-3 text-sm text-muted-foreground">
      Si no ves el correo en tu bandeja de entrada, revisa también Spam o correo no deseado.
    </p>
  )
}
