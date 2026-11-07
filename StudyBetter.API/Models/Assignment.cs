
namespace StudyBetter.API.Models
{
    public class Assignment
    {
        public int AssignmentID { get; set; }

        public int UserID { get; set; }

        // Optional relationship with a module
        public int? ModuleID { get; set; }

        public string Title { get; set; } = string.Empty;

        public string Description { get; set; } = string.Empty;

        // Retains compatibility with the existing Android app
        public string ModuleName { get; set; } = "General";

        // Stored in milliseconds, matching the Android implementation
        public long DueDateMillis { get; set; }

        public string Priority { get; set; } = "Medium";

        public int Progress { get; set; }

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

        public User? User { get; set; }

        public Module? Module { get; set; }
    }
}
