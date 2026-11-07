
using System.ComponentModel.DataAnnotations;

namespace StudyBetter.API.DTOs
{
    public class AssignmentRequest
    {
        [Required]
        [MaxLength(150)]
        public string Title { get; set; } = string.Empty;

        public string Description { get; set; } = string.Empty;

        public int? ModuleID { get; set; }

        public string ModuleName { get; set; } = "General";

        public long DueDateMillis { get; set; }

        public string Priority { get; set; } = "Medium";

        [Range(0, 100)]
        public int Progress { get; set; }
    }
}
