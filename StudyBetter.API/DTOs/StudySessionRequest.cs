
using System.ComponentModel.DataAnnotations;

namespace StudyBetter.API.DTOs
{
    public class StudySessionRequest
    {
        public DateTime StartedAt { get; set; }

        [Range(1, 86400)]
        public int DurationSeconds { get; set; }

        [MaxLength(250)]
        public string Description { get; set; } = string.Empty;
    }
}
