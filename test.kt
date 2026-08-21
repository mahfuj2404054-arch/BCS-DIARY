import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
fun test() {
    val b = BorderStroke(1.dp, Color.Black)
    b.copy(brush = androidx.compose.ui.graphics.SolidColor(Color.White))
}
