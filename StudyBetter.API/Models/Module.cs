
namespace StudyBetter.API.Models
{
    public class Module
    {
        public int ModuleID { get; set; }

        public int UserID { get; set; }

        public string Code { get; set; } = string.Empty;

        public string Title { get; set; } = string.Empty;

        public string Description { get; set; } = string.Empty;

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

        // Relationship with the user who owns this module
        public User? User { get; set; }
    }
}
