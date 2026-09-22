package com.chemscanner.omniscient.utils

import androidx.compose.ui.graphics.Color

/**
 * Coerces the alpha value of the color between [min] and [max].
 */
fun Color.coerceIn(min: Float, max: Float): Color {
    return this.copy(alpha = this.alpha.coerceIn(min, max))
}
