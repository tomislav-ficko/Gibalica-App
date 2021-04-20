package hr.fer.tel.gibalica.utils

data class NotificationEvent(
    val eventType: EventType
)

enum class EventType {
    COUNTER_FINISHED,
    DETECTED_LEFT_HAND,
    DETECTED_RIGHT_HAND,
    DETECTED_BOTH_HANDS,
    DETECTED_T_POSE,
    DETECTED_SQUAT,
    DETECTED_STARTING_POSE,
    DETECTED_ALL_JOINTS_VISIBLE
}
