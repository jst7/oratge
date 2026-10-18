import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';

/** Texto configurable del diálogo, para poder reutilizarlo en otras confirmaciones. */
export interface DatosDialogoConfirmacion {
  readonly titulo: string;
  readonly textoConfirmar: string;
  readonly textoCancelar: string;
}

/**
 * Diálogo de confirmación genérico. Devuelve `true` si el usuario confirma
 * y `false`/`undefined` si cancela o cierra.
 */
@Component({
  selector: 'app-dialogo-confirmacion',
  imports: [MatButtonModule, MatDialogModule],
  templateUrl: './dialogo-confirmacion.html',
  styleUrl: './dialogo-confirmacion.scss',
})
export class DialogoConfirmacion {
  protected readonly datos = inject<DatosDialogoConfirmacion>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<DialogoConfirmacion, boolean>);

  protected cancelar(): void {
    this.dialogRef.close(false);
  }

  protected confirmar(): void {
    this.dialogRef.close(true);
  }
}
