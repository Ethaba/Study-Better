
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using StudyBetter.API.Data;
using StudyBetter.API.DTOs;
using StudyBetter.API.Models;

namespace StudyBetter.API.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/[controller]")]
    public class AssignmentsController : ControllerBase
    {
        private readonly StudyBetterDbContext _context;

        public AssignmentsController(StudyBetterDbContext context)
        {
            _context = context;
        }

        private int? GetCurrentUserId()
        {
            string? userId =
                User.FindFirst(ClaimTypes.NameIdentifier)?.Value ??
                User.FindFirst(JwtRegisteredClaimNames.Sub)?.Value;

            if (int.TryParse(userId, out int id))
                return id;

            return null;
        }

        // GET: api/Assignments
        [HttpGet]
        public async Task<IActionResult> GetAssignments()
        {
            var userId = GetCurrentUserId();

            if (userId == null)
                return Unauthorized();

            var assignments = await _context.Assignments
                .Where(a => a.UserID == userId.Value)
                .OrderBy(a => a.DueDateMillis)
                .Select(a => new AssignmentResponse
                {
                    AssignmentID = a.AssignmentID,
                    Title = a.Title,
                    Description = a.Description,
                    ModuleID = a.ModuleID,
                    ModuleName = a.ModuleName,
                    DueDateMillis = a.DueDateMillis,
                    Priority = a.Priority,
                    Progress = a.Progress,
                    CreatedAt = a.CreatedAt,
                    UpdatedAt = a.UpdatedAt
                })
                .ToListAsync();

            return Ok(assignments);
        }

        // GET: api/Assignments/5
        [HttpGet("{id}")]
        public async Task<IActionResult> GetAssignment(int id)
        {
            var userId = GetCurrentUserId();

            if (userId == null)
                return Unauthorized();

            var assignment = await _context.Assignments
                .Where(a => a.AssignmentID == id &&
                            a.UserID == userId.Value)
                .Select(a => new AssignmentResponse
                {
                    AssignmentID = a.AssignmentID,
                    Title = a.Title,
                    Description = a.Description,
                    ModuleID = a.ModuleID,
                    ModuleName = a.ModuleName,
                    DueDateMillis = a.DueDateMillis,
                    Priority = a.Priority,
                    Progress = a.Progress,
                    CreatedAt = a.CreatedAt,
                    UpdatedAt = a.UpdatedAt
                })
                .FirstOrDefaultAsync();

            if (assignment == null)
                return NotFound(new { message = "Assignment not found." });

            return Ok(assignment);
        }

        // POST: api/Assignments
        [HttpPost]
        public async Task<IActionResult> CreateAssignment(
            [FromBody] AssignmentRequest request)
        {
            var userId = GetCurrentUserId();

            if (userId == null)
                return Unauthorized();

            string title = request.Title.Trim();

            if (string.IsNullOrWhiteSpace(title))
                return BadRequest(new { message = "Assignment title is required." });

            if (request.Progress < 0 || request.Progress > 100)
                return BadRequest(new { message = "Progress must be between 0 and 100." });

            var moduleName = request.ModuleName?.Trim() ?? "General";

            if (request.ModuleID.HasValue)
            {
                var module = await _context.Modules.FirstOrDefaultAsync(
                    m => m.ModuleID == request.ModuleID.Value &&
                         m.UserID == userId.Value);

                if (module == null)
                    return BadRequest(new { message = "The selected module was not found." });

                moduleName = module.Title;
            }

            var assignment = new Assignment
            {
                UserID = userId.Value,
                ModuleID = request.ModuleID,
                Title = title,
                Description = request.Description?.Trim() ?? string.Empty,
                ModuleName = string.IsNullOrWhiteSpace(moduleName) ? "General" : moduleName,
                DueDateMillis = request.DueDateMillis,
                Priority = request.Priority?.Trim() ?? "Medium",
                Progress = request.Progress,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };

            _context.Assignments.Add(assignment);
            await _context.SaveChangesAsync();

            return CreatedAtAction(
                nameof(GetAssignment),
                new { id = assignment.AssignmentID },
                ToResponse(assignment));
        }

        // PUT: api/Assignments/5
        [HttpPut("{id}")]
        public async Task<IActionResult> UpdateAssignment(
            int id,
            [FromBody] AssignmentRequest request)
        {
            var userId = GetCurrentUserId();

            if (userId == null)
                return Unauthorized();

            var assignment = await _context.Assignments.FirstOrDefaultAsync(
                a => a.AssignmentID == id &&
                     a.UserID == userId.Value);

            if (assignment == null)
                return NotFound(new { message = "Assignment not found." });

            if (string.IsNullOrWhiteSpace(request.Title))
                return BadRequest(new { message = "Assignment title is required." });

            if (request.Progress < 0 || request.Progress > 100)
                return BadRequest(new { message = "Progress must be between 0 and 100." });

            var moduleName = request.ModuleName?.Trim() ?? "General";

            if (request.ModuleID.HasValue)
            {
                var module = await _context.Modules.FirstOrDefaultAsync(
                    m => m.ModuleID == request.ModuleID.Value &&
                         m.UserID == userId.Value);

                if (module == null)
                    return BadRequest(new { message = "The selected module was not found." });

                moduleName = module.Title;
            }

            assignment.Title = request.Title.Trim();
            assignment.Description = request.Description?.Trim() ?? string.Empty;
            assignment.ModuleID = request.ModuleID;
            assignment.ModuleName = string.IsNullOrWhiteSpace(moduleName) ? "General" : moduleName;
            assignment.DueDateMillis = request.DueDateMillis;
            assignment.Priority = request.Priority?.Trim() ?? "Medium";
            assignment.Progress = request.Progress;
            assignment.UpdatedAt = DateTime.UtcNow;

            await _context.SaveChangesAsync();

            return Ok(ToResponse(assignment));
        }

        // DELETE: api/Assignments/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteAssignment(int id)
        {
            var userId = GetCurrentUserId();

            if (userId == null)
                return Unauthorized();

            var assignment = await _context.Assignments.FirstOrDefaultAsync(
                a => a.AssignmentID == id &&
                     a.UserID == userId.Value);

            if (assignment == null)
                return NotFound(new { message = "Assignment not found." });

            _context.Assignments.Remove(assignment);
            await _context.SaveChangesAsync();

            return Ok(new { message = "Assignment deleted successfully." });
        }

        private static AssignmentResponse ToResponse(Assignment assignment)
        {
            return new AssignmentResponse
            {
                AssignmentID = assignment.AssignmentID,
                Title = assignment.Title,
                Description = assignment.Description,
                ModuleID = assignment.ModuleID,
                ModuleName = assignment.ModuleName,
                DueDateMillis = assignment.DueDateMillis,
                Priority = assignment.Priority,
                Progress = assignment.Progress,
                CreatedAt = assignment.CreatedAt,
                UpdatedAt = assignment.UpdatedAt
            };
        }
    }
}
