/**
 * Días vista que admite la API: 0 = hoy, hasta 3 días por delante.
 * El límite lo marca AEMET, que más allá no publica probabilidad de
 * precipitación.
 */
export const DIAS_VISTA = [0, 1, 2, 3] as const;

export type DiaVista = (typeof DIAS_VISTA)[number];

export const DIA_VISTA_POR_DEFECTO: DiaVista = 0;
