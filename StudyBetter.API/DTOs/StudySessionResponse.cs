
namespace StudyBetter.API.DTOs
{
    public class StudySessionResponse
    {
        public int StudySessionID { get; set; }
        public DateTime StartedAt { get; set; }
        public int DurationSeconds { get; set; }
        public string Description { get; set; } = string.Empty;
        public DateTime CreatedAt { get; set; }
    }
}
