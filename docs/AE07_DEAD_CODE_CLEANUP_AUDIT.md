# AE07 — Auditoría de código candidato a eliminación

Se localizaron archivos de respaldo bajo `frontdesk-service/src/main/java` y
superficies hoteleras activamente referenciadas por frontend, clientes HTTP y
pruebas. Un archivo que parece legado por nombre no es eliminable sin confirmar
que no participa en compilación, empaquetado o documentación operativa.

## Resultado de la limpieza segura

Se localizaron dos copias `AssistantController.java.backup-*` dentro de
`frontdesk-service/src/main/java`. La comprobación de referencias por nombre y
la inspección del source set demostraron que no eran clases Java compilables,
no estaban importadas ni referenciadas por configuración, pruebas o
documentación operativa. Se eliminaron únicamente esas dos copias; no se
tocaron las clases activas ni los artefactos generados bajo `bin/`.

Las clases y rutas hoteleras que sí tienen consumidores permanecen retenidas.
Su eliminación exige completar la migración descrita en AE02–AE04 y obtener
evidencia de cero consumidores.
