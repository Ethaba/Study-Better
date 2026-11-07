
using Microsoft.EntityFrameworkCore;
using StudyBetter.API.Models;

namespace StudyBetter.API.Data
{
    public class StudyBetterDbContext : DbContext
    {
        public StudyBetterDbContext(DbContextOptions<StudyBetterDbContext> options)
            : base(options)
        {
        }

        public DbSet<User> Users => Set<User>();

        public DbSet<Module> Modules => Set<Module>();

        public DbSet<Assignment> Assignments => Set<Assignment>();

        public DbSet<StudySession> StudySessions => Set<StudySession>();

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            // User configuration
            modelBuilder.Entity<User>()
                .HasIndex(u => u.Email)
                .IsUnique();

            // Module configuration
            modelBuilder.Entity<Module>()
                .HasOne(m => m.User)
                .WithMany()
                .HasForeignKey(m => m.UserID)
                .OnDelete(DeleteBehavior.Cascade);

            // Assignment configuration
            modelBuilder.Entity<Assignment>()
                .HasOne(a => a.User)
                .WithMany()
                .HasForeignKey(a => a.UserID)
                .OnDelete(DeleteBehavior.NoAction);

            modelBuilder.Entity<Assignment>()
                .HasOne(a => a.Module)
                .WithMany()
                .HasForeignKey(a => a.ModuleID)
                .OnDelete(DeleteBehavior.SetNull);

            // Study session configuration
            modelBuilder.Entity<StudySession>()
                .HasOne(s => s.User)
                .WithMany()
                .HasForeignKey(s => s.UserID)
                .OnDelete(DeleteBehavior.Cascade);

            // Prevent duplicate module codes for the same user
            modelBuilder.Entity<Module>()
                .HasIndex(m => new { m.UserID, m.Code })
                .IsUnique();

            // Improve queries that retrieve records belonging to a user
            modelBuilder.Entity<Assignment>()
                .HasIndex(a => a.UserID);

            modelBuilder.Entity<StudySession>()
                .HasIndex(s => new { s.UserID, s.StartedAt });
        }
    }
}
