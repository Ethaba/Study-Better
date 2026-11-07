namespace StudyBetter.API.Models
{
    public class User
    {
        public int UserID { get; set; }

        public string FullName { get; set; } = string.Empty;

        public string Email { get; set; } = string.Empty;

        public string PasswordHash { get; set; } = string.Empty;

        public string Language { get; set; } = "English";

        public int StudyGoal { get; set; } = 60;

        public string Theme { get; set; } = "Light";

        public bool NotificationsEnabled { get; set; } = true;

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    }
}
