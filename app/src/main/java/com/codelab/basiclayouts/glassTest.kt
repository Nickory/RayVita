
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun GlassEffectBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    blurRadius: Dp = 30.dp,
    backgroundColor: Color = Color.White.copy(alpha = 0.1f),
    borderColor: Color = Color.White.copy(alpha = 0.4f),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .graphicsLayer {

            }
            .drawBehind {
                // 背景色
                drawRoundRect(
                    color = backgroundColor,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx())
                )
                // 高光边框
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )
            }
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun GlassEffectBoxPreview() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // 整体加一个漂亮的渐变背景
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF89F7FE), // 淡蓝
                            Color(0xFF66A6FF)  // 深蓝
                        )
                    )
                )
                .padding(32.dp)
        ) {
            GlassEffectBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                cornerRadius = 32.dp,
                blurRadius = 40.dp,
                backgroundColor = Color.White.copy(alpha = 0.08f),
                borderColor = Color.White.copy(alpha = 0.4f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Glass Card",
                        color = Color.Black.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Beautiful frosted glass effect!",
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
                .padding(32.dp)
        ) {
            Text(
                text = "Preview - Not Supported (API < 31)",
                color = Color.Black
            )
        }
    }
}
