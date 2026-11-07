
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
    public class ModulesController : ControllerBase
    {
        private readonly StudyBetterDbContext _context;

        public ModulesController(StudyBetterDbContext context)
        {
            _context = context;
        }

        // Gets the ID of the currently authenticated user.
        private int? GetCurrentUserId()
        {
            string? userId =
                User.FindFirst(ClaimTypes.NameIdentifier)?.Value ??
                User.FindFirst(JwtRegisteredClaimNames.Sub)?.Value;

            if (int.TryParse(userId, out int id))
            {
                return id;
            }

            return null;
        }

        // GET: api/Modules
        [HttpGet]
        public async Task<IActionResult> GetModules()
        {
            var userId = GetCurrentUserId();

            if (userId == null)
                return Unauthorized();

            var modules = await _context.Modules
                .Where(m => m.UserID == userId.Value)
                .OrderBy(m => m.Title)
                .Select(m => new ModuleResponse
                {
                    ModuleID = m.ModuleID,
                    Code = m.Code,
                    Title = m.Title,
                    Description = m.Description,
                    CreatedAt = m.CreatedAt,
                    UpdatedAt = m.UpdatedAt
                })
                .ToListAsync();

            return Ok(modules);
        }

        // GET: api/Modules/5
        [HttpGet("{id}")]
        public async Task<IActionResult> GetModule(int id)
        {
            var userId = GetCurrentUserId();

            if (userId == null)
                return Unauthorized();

            var module = await _context.Modules
                .Where(m => m.ModuleID == id &&
                            m.UserID == userId.Value)
                .Select(m => new ModuleResponse
                {
                    ModuleID = m.ModuleID,
                    Code = m.Code,
                    Title = m.Title,
                    Description = m.Description,
                    CreatedAt = m.CreatedAt,
                    UpdatedAt = m.UpdatedAt
                })
                .FirstOrDefaultAsync();

            if (module == null)
                return NotFound(new { message = "Module not found." });

            return Ok(module);
        }

        // POST: api/Modules
        [HttpPost]
        public async Task<IActionResult> CreateModule(
            [FromBody] ModuleRequest request)
        {
            var userId = GetCurrentUserId();

            if (userId == null)
                return Unauthorized();

            string code = request.Code.Trim();
            string title = request.Title.Trim();

            if (string.IsNullOrWhiteSpace(code) ||
                string.IsNullOrWhiteSpace(title))
            {
                return BadRequest(new
                {
                    message = "Module code and title are required."
                });
            }

            bool codeExists = await _context.Modules.AnyAsync(
                m => m.UserID == userId.Value &&
                     m.Code == code);

            if (codeExists)
            {
                return Conflict(new
                {
                    message = "You already have a module with this code."
                });
            }

            var module = new Module
            {
                UserID = userId.Value,
                Code = code,
                Title = title,
                Description = request.Description.Trim(),
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };

            _context.Modules.Add(module);
            await _context.SaveChangesAsync();

            var response = new ModuleResponse
            {
                ModuleID = module.ModuleID,
                Code = module.Code,
                Title = module.Title,
                Description = module.Description,
                CreatedAt = module.CreatedAt,
                UpdatedAt = module.UpdatedAt
            };

            return CreatedAtAction(
                nameof(GetModule),
                new { id = module.ModuleID },
                response);
        }

        // PUT: api/Modules/5
        [HttpPut("{id}")]
        public async Task<IActionResult> UpdateModule(
            int id,
            [FromBody] ModuleRequest request)
        {
            var userId = GetCurrentUserId();

            if (userId == null)
                return Unauthorized();

            var module = await _context.Modules.FirstOrDefaultAsync(
                m => m.ModuleID == id &&
                     m.UserID == userId.Value);

            if (module == null)
                return NotFound(new { message = "Module not found." });

            string code = request.Code.Trim();
            string title = request.Title.Trim();

            if (string.IsNullOrWhiteSpace(code) ||
                string.IsNullOrWhiteSpace(title))
            {
                return BadRequest(new
                {
                    message = "Module code and title are required."
                });
            }

            bool codeExists = await _context.Modules.AnyAsync(
                m => m.UserID == userId.Value &&
                     m.Code == code &&
                     m.ModuleID != id);

            if (codeExists)
            {
                return Conflict(new
                {
                    message = "Another module already uses this code."
                });
            }

            module.Code = code;
            module.Title = title;
            module.Description = request.Description.Trim();
            module.UpdatedAt = DateTime.UtcNow;

            await _context.SaveChangesAsync();

            return Ok(new ModuleResponse
            {
                ModuleID = module.ModuleID,
                Code = module.Code,
                Title = module.Title,
                Description = module.Description,
                CreatedAt = module.CreatedAt,
                UpdatedAt = module.UpdatedAt
            });
        }

        // DELETE: api/Modules/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteModule(int id)
        {
            var userId = GetCurrentUserId();

            if (userId == null)
                return Unauthorized();

            var module = await _context.Modules.FirstOrDefaultAsync(
                m => m.ModuleID == id &&
                     m.UserID == userId.Value);

            if (module == null)
                return NotFound(new { message = "Module not found." });

            _context.Modules.Remove(module);
            await _context.SaveChangesAsync();

            return Ok(new
            {
                message = "Module deleted successfully."
            });
        }
    }
}
