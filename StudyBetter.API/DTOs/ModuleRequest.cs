using System.ComponentModel.DataAnnotations;

namespace StudyBetter.API.DTOs
{
    public class ModuleRequest
    {
        [Required]
        [MaxLength(50)]
        public string Code { get; set; } = string.Empty;

        [Required]
        [MaxLength(150)]
        public string Title { get; set; } = string.Empty;

        public string Description { get; set; } = string.Empty;
    }
}