sed -i '1d;2d;3d' app/src/main/java/com/example/ui/viewmodel/StudyViewModel.kt
sed -i '/import android.app.Application/i \
import com.google.firebase.storage.FirebaseStorage\
import android.net.Uri\
import kotlinx.coroutines.tasks.await' app/src/main/java/com/example/ui/viewmodel/StudyViewModel.kt
