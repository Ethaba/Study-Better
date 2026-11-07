
namespace StudyBetter.API.Models
{
    public class StudySession
    {
        public int StudySessionID { get; set; }

        public int UserID { get; set; }

        // When the study session started
        public DateTime StartedAt { get; set; }

        // Actual duration studied, in seconds
        public int DurationSeconds { get; set; }

        // Optional description of what the user studied
        public string Description { get; set; } = string.Empty;

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        public User? User { get; set; }
    }
}
