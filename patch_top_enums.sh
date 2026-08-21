sed -i '/import com.example.data.db.AppDatabase/a \
import com.example.data.firebase.AuthResultState\
import com.example.data.firebase.FirebaseAuthService\
import com.example.data.firebase.FirestoreSyncService\
import com.google.firebase.auth.FirebaseAuth\
import com.example.data.model.RepeatSchedule\
import com.example.data.model.SubjectEntity\
import com.example.data.model.SubjectWithStats\
import com.example.data.model.TaskCompletionEntity\
import com.example.data.model.TaskEntity\
import com.example.data.model.TaskPriority\
import com.example.data.model.TaskWithDetails\
import com.example.data.model.TopicEntity\
import com.example.data.model.TopicWithStats\
import com.example.data.model.UserEntity\
import com.example.data.model.UserRole\
import com.example.data.repository.StudyRepository\
import com.example.ui.components.CelebrationData\
import com.example.ui.theme.ThemeStyle\
import kotlinx.coroutines.Dispatchers\
import kotlinx.coroutines.ExperimentalCoroutinesApi\
import kotlinx.coroutines.flow.MutableSharedFlow\
import kotlinx.coroutines.flow.MutableStateFlow\
import kotlinx.coroutines.flow.SharedFlow\
import kotlinx.coroutines.flow.SharingStarted\
import kotlinx.coroutines.flow.StateFlow\
import kotlinx.coroutines.flow.asSharedFlow\
import kotlinx.coroutines.flow.asStateFlow\
import kotlinx.coroutines.flow.combine\
import kotlinx.coroutines.flow.flatMapLatest\
import kotlinx.coroutines.flow.flowOn\
import kotlinx.coroutines.flow.stateIn\
import kotlinx.coroutines.launch\
import java.text.SimpleDateFormat\
import java.util.Date\
import java.util.Locale\
import java.util.UUID\
\
sealed class UiEvent {\
    data class ShowToast(val message: String) : UiEvent()\
    data class NavigateToTaskDetail(val taskId: String) : UiEvent()\
    object TaskCreated : UiEvent()\
}\
\
enum class StudentTab {\
    TASKS,\
    TOPICS,\
    SUBJECTS,\
    EXAMS\
}\
\
enum class TaskFilter {\
    ALL,\
    TODAY,\
    PENDING,\
    COMPLETED\
}\
' app/src/main/java/com/example/ui/viewmodel/StudyViewModel.kt
