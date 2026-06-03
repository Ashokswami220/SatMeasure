import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
fun test() {
    BottomSheetScaffold(
        sheetMaxWidth = 400.dp,
        sheetContent = {}
    ) {}
}
