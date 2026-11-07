
namespace StudyBetter.API.DTOs
{
    public class AssignmentResponse
    {
        public int AssignmentID { get; set; }
        public string Title { get; set; } = string.Empty;
        public string Description { get; set; } = string.Empty;
        public int? ModuleID { get; set; }
        public string ModuleName { get; set; } = "General";
        public long DueDateMillis { get; set; }
        public string Priority { get; set; } = "Medium";
        public int Progress { get; set; }
        public DateTime CreatedAt { get; set; }
        public DateTime UpdatedAt { get; set; }
    }
}
