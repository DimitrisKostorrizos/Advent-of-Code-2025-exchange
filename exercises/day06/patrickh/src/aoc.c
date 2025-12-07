/*
 * aoc.c
 *
 *  Created on: Dec 2, 2024
 *      Author: pat
 */

#include "aoc.h"

#include <bits/stdint-intn.h>
#include <bits/stdint-uintn.h>
#include <bits/types/clock_t.h>
#include <bits/types/FILE.h>
#include <ctype.h>
#include <stdarg.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include <math.h>
#include <time.h>

#include "color.h"
#include "interactive.h"

#ifdef INTERACTIVE
#define INTERACT(...) __VA_ARGS__
#else
#define INTERACT(...)
#endif

struct data* read_data(const char *path);

int year = 2025;
int day = 6;
int part = 2;
FILE *solution_out;
#ifdef INTERACTIVE
int interactive = 0;
#else
#define interactive 0
#endif

#define starts_with(str, start) !memcmp(str, start, sizeof(start) - 1)

typedef size_t idx;
typedef off_t pos;

#define NUM_MAX UINT16_MAX
typedef uint16_t num;

struct data {
	size_t column_count;
	size_t line_count;
	size_t line_length;
	size_t line_alloc;
	char *lines2;
	num *lines;
	idx *column_begin;
	_Bool *ops_multiply;
	uint64_t *solutions;
};

static int do_print = 1;

#if 0
static void print_step(FILE *str, uint64_t result, char *format, ...) __attribute__ ((__format__ (__printf__, 3, 4)));

static void print_step(FILE *str, uint64_t result, char *format, ...) {
	if (result) {
		fprintf(str, "%sresult=%"I64"u\n%s", STEP_HEADER, result, STEP_BODY);
	} else {
		fputs(STEP_BODY, str);
	}
	if (!do_print && !interactive) {
		return;
	}
	va_list list;
	va_start(list, format);
	vfprintf(str, format, list);
	if (interactive)
		fputs(STEP_FINISHED, str);
}
#endif

static void print_space(FILE *str, uint64_t count) {
	uint64_t val;
	for (val = 0; val + INT_MAX < count; val += INT_MAX)
		fprintf(str, "%*s", INT_MAX, "");
	fprintf(str, "%*s", (int) (count - val), "");
}

static void print(FILE *str, struct data *data, uint64_t result, idx curi) {
	if (result)
		fprintf(str, "%sresult=%"I64"u\n%s", STEP_HEADER, result, STEP_BODY);
	else
		fputs(STEP_BODY, str);
	if (!do_print && !interactive)
		return;
	uint64_t *add_col_len = calloc(data->column_count, sizeof(uint64_t));
	for (idx c = 0; c + 1 < data->column_count && data->column_begin[c + 1];
			++c) {
		size_t min_len = log10(data->solutions[c]) + 2;
		size_t cur_len = data->column_begin[c + 1] - data->column_begin[c];
		if (cur_len < min_len)
			add_col_len[c] = min_len - cur_len;
	}
	for (idx l = 0; l < data->line_count + 1; ++l) {
		char *line_start = data->lines2 + l * data->line_length;
		idx last_end = 0;
		for (idx c = 0; c + 1 < data->column_count && data->column_begin[c + 1];
				++c) {
			idx next = data->column_begin[c + 1];
			fwrite(line_start + last_end, next - last_end, 1, str);
			print_space(str, add_col_len[c]);
			last_end = next;
		}
		idx next = data->line_length;
		if (curi != UINT64_MAX && part == 2 && l != data->line_count) {
			if (curi < last_end)
				abort();
			fwrite(line_start + last_end, curi - last_end, 1, str);
			fprintf(str, BOLD FC_GREEN"%c"RESET, (unsigned) line_start[curi]);
			fwrite(line_start + curi + 1, data->line_length - curi - 1, 1, str);
		} else if (part == 1 && curi == l) {
			char *num_end;
			strtoll(line_start + last_end, &num_end, 10);
			fputs(BOLD FC_GREEN, str);
			fwrite(line_start + last_end, num_end - line_start - last_end, 1, str);
			fputs(RESET, str);
			fwrite(num_end, line_start + next - num_end, 1, str);
		} else
			fwrite(line_start + last_end, next - last_end, 1, str);
		fputc('\n', str);
	}
	idx pos = 0;
	for (idx c = 0; c < data->column_count; ++c) {
		pos += fprintf(str, "%"I64"u", data->solutions[c]);
		idx next = data->column_begin[c + 1];
		if (!next)
			break;
		print_space(str, add_col_len[c] + next - pos);
		pos = next;
	}
	fputc('\n', str);
	free(add_col_len);
	fputs(interactive ? STEP_FINISHED : RESET, str);
}

static num parse_num(struct data *data, idx ci) {
	num result = 0;
	int any_input = 0;
	int last_input = 0;
	for (idx l = 0; l < data->line_count; ++l) {
		char chr = data->lines2[l * data->line_length + ci];
		if (chr >= '0' && chr <= '9') {
			if (!any_input) {
				any_input = 1;
				last_input = 1;
			} else if (!last_input)
				(fprintf(stderr, "chr=%02X, ci=%"I64"u, ll=%lu\n",
						(unsigned) chr, ci, data->line_length), abort());
			result = result * 10 + chr - '0';
		} else if (chr != ' ')
			(fprintf(stderr, "chr=%02X, ci=%"I64"u, ll=%lu\n", (unsigned) chr,
					ci, data->line_length), abort());
		else
			last_input = 0;
	}
	if (any_input)
		return result;
	return NUM_MAX;
}

const char* solve(const char *path) {
	struct data *data = read_data(path);
	uint64_t result = 0;
	if (part == 1) {
		for (idx c = 0, ci = 0, nci = 0; c < data->column_count;
				++c, ci = nci) {
			int had_valid = 0;
			for (; nci < data->line_length; nci++) {
				if (parse_num(data, nci) == NUM_MAX) {
					if (had_valid)
						break;
					++ci;
					continue;
				}
				had_valid = 1;
			}
			data->column_begin[c] = ci;
			uint64_t solution = data->ops_multiply[c];
			for (idx l = 0; l < data->line_count; ++l) {
				if (data->ops_multiply[c])
					solution *= data->lines[l * data->column_count + c];
				else
					solution += data->lines[l * data->column_count + c];
				data->solutions[c] = solution;
				print(solution_out, data, result + solution, l);
			}
			result += solution;
			data->solutions[c] = solution;
			print(solution_out, data, result, UINT64_MAX);
		}
	} else {
		idx ci = 0;
		for (idx cn = 0; cn < data->column_count; ++cn) {
			uint64_t solution = data->ops_multiply[cn];
			int had_valid = 0;
			while (119) {
				num num = parse_num(data, ci);
				if (num == NUM_MAX) {
					++ci;
					if (had_valid)
						break;
					continue;
				}
				if (!had_valid) {
					had_valid = 1;
					data->column_begin[cn] = ci;
				}
				if (data->ops_multiply[cn])
					solution *= num;
				else
					solution += num;
				data->solutions[cn] = solution;
				print(solution_out, data, result + solution, ci);
				if (++ci == data->line_length)
					break;
			}
			result += solution;
			data->solutions[cn] = solution;
			print(solution_out, data, result, UINT64_MAX);
		}
		if (ci != data->line_length)
			abort();
	}
	print(solution_out, data, result, UINT64_MAX);
	free(data);
	return u64toa(result);
}

static struct data* parse_line(struct data *data, char *line) {
	for (char *p = line; 171; ++p) {
		if (!*p)
			return data;
		else if (!isspace(*p))
			break;
	}

	if (!data) {
		data = calloc(1, sizeof(struct data));
	}
	if (!data->column_count) {
		char *p = line;
		do {
			for (; *p && isspace(*p); ++p)
				;
			if (!isdigit(*p))
				break;
			for (; *p && isdigit(*p); ++p)
				;
			data->column_count++;
		} while (*p);
	}
	if (data->line_count == data->line_alloc) {
		data->line_alloc += 64;
		data->lines = reallocarray(data->lines, data->line_alloc,
				data->column_count * sizeof(num));
		data->lines2 = reallocarray(data->lines2, data->line_alloc,
				data->line_length);
	}
	if (data->ops_multiply)
		abort();
	char *p = line, *end = NULL;
	idx lc = data->line_count;
	if (*line != '*' && *line != '+') {
		for (idx i = 0; i < data->column_count; ++i) {
			long long val = strtoll(p, &end, 10);
			if (errno || p == end)
				abort();
			p = end;
			if (val < 0 || val >= NUM_MAX)
				abort();
			data->lines[data->line_count * data->column_count + i] = val;
		}
		for (; *p && isspace(*p); ++p)
			;
		data->line_count++;
	} else {
		data->ops_multiply = malloc(data->column_count);
		data->solutions = calloc(data->column_count, sizeof(uint64_t));
		data->column_begin = calloc(data->column_count, sizeof(idx));
		for (idx i = 0; i < data->column_count; ++i) {
			data->ops_multiply[i] = *p == '*' ? 1 :
									*p == '+' ? 0 : (abort(), 0);
			end = p + 1;
			for (++p; *p && isspace(*p); ++p)
				;
		}
	}
	size_t ll = end - line;
	if (ll > data->line_length) {
		data->lines2 = reallocarray(data->lines2, data->line_alloc, ll);
		for (int l = lc; --l >= 0;) {
			memmove(data->lines2 + l * ll, data->lines2 + l * data->line_length,
					data->line_length);
			memset(data->lines2 + l * ll + data->line_length, ' ',
					ll - data->line_length);
		}
		data->line_length = ll;
	}
	memcpy(data->lines2 + lc * data->line_length, line, ll);
	memset(data->lines2 + lc * data->line_length + ll, ' ',
			data->line_length - ll);
	if (*p)
		abort();
	return data;
}

// common stuff

#if !(AOC_COMPAT & AC_POSIX)
ssize_t getline(char **line_buf, size_t *line_len, FILE *file) {
	ssize_t result = 0;
	while (21) {
		if (*line_len == result) {
			size_t len = result ? result * 2 : 64;
			void *ptr = realloc(*line_buf, len);
			if (!ptr) {
				fseek(file, -result, SEEK_CUR);
				return -1;
			}
			*line_len = len;
			*line_buf = ptr;
		}
		ssize_t len = fread(*line_buf + result, 1, *line_len - result, file);
		if (!len) {
			if (!result) {
				return -1;
			}
			if (result == *line_len) {
				void *ptr = realloc(*line_buf, result + 1);
				if (!ptr) {
					fseek(file, -result, SEEK_CUR);
					return -1;
				}
				*line_len = result + 1;
				*line_buf = ptr;
			}
			(*line_buf)[result] = 0;
			return result;
		}
		char *c = memchr(*line_buf + result, '\n', len);
		if (c) {
			ssize_t result2 = c - *line_buf + 1;
			if (result2 == *line_len) {
				void *ptr = realloc(*line_buf, result2 + 1);
				if (!ptr) {
					fseek(file, -*line_len - len, SEEK_CUR);
					return -1;
				}
				*line_len = result2 + 1;
				*line_buf = ptr;
			}
			fseek(file, result2 - result - len, SEEK_CUR);
			(*line_buf)[result2] = 0;
			return result2;
		}
		result += len;
	}
}
#endif // AC_POSIX
#if !(AOC_COMPAT & AC_STRCN)
char* strchrnul(char *str, int c) {
	char *end = strchr(str, c);
	return end ? end : (str + strlen(str));
}
#endif // AC_STRCN
#if !(AOC_COMPAT & AC_REARR)
void* reallocarray(void *ptr, size_t nmemb, size_t size) {
	size_t s = nmemb * size;
	if (s / size != nmemb) {
		errno = ENOMEM;
		return 0;
	}
	return realloc(ptr, s);
}
#endif // AC_REARR

char* u64toa(uint64_t value) {
	static char result[21];
	if (sprintf(result, "%"I64"u", value) <= 0) {
		return 0;
	}
	return result;
}

char* d64toa(int64_t value) {
	static char result[21];
	if (sprintf(result, "%"I64"d", value) <= 0) {
		return 0;
	}
	return result;
}

struct data* read_data(const char *path) {
	char *line_buf = 0;
	size_t line_len = 0;
	struct data *result = 0;
	FILE *file = fopen(path, "rb");
	if (!file) {
		perror("fopen");
		abort();
	}
	while (123) {
		ssize_t s = getline(&line_buf, &line_len, file);
		if (s < 0) {
			if (feof(file)) {
				free(line_buf);
				fclose(file);
				return result;
			}
			perror("getline failed");
			fflush(0);
			abort();
		}
		if (strlen(line_buf) != s) {
			fprintf(stderr, "\\0 character in line!");
			abort();
		}
		result = parse_line(result, line_buf);
	}
}

int main(int argc, char **argv) {
#ifdef INTERACTIVE
	int force_non_interactive = 0;
#endif
	solution_out = stdout;
	char *me = argv[0];
	char *f = 0;
	if (argc > 1) {
		if (argc > 4) {
			print_help: ;
			fprintf(stderr, "usage: %s"
#ifdef INTERACTIVE
							" [[non-]interactive]"
#else
							" [non-interactive]"

#endif
					/*	  */" [p1|p2] [DATA]\n", me);
			return 1;
		}
		int idx = 1;
		if (!strcmp("help", argv[idx])) {
			goto print_help;
		}
		if (!strcmp("non-interactive", argv[idx])) {
			idx++;
#ifdef INTERACTIVE
			force_non_interactive = 1;
#endif
		}
#ifdef INTERACTIVE
		else if (!strcmp("interactive", argv[idx])) {
			idx++;
			interactive = 1;
		}
#endif
		if (idx < argc) {
			if (!strcmp("p1", argv[idx])) {
				part = 1;
				idx++;
			} else if (!strcmp("p2", argv[idx])) {
				part = 2;
				idx++;
			}
			if (!f && argv[idx]) {
				f = argv[idx++];
			}
			if (f && argv[idx]) {
				goto print_help;
			}
		}
	}
	if (!f) {
		f = "rsrc/data.txt";
	} else if (!strchr(f, '/')) {
		char *f2 = malloc(64);
		if (snprintf(f2, 64, "rsrc/test%s.txt", f) <= 0) {
			perror("snprintf");
			abort();
		}
		f = f2;
	}
#ifdef INTERACTIVE
	if (interactive) {
		printf("execute now day %d part %d on file %s in interactive mode\n",
				day, part, f);
	}
	if (!force_non_interactive) {
		interact(f, interactive);
	}
#endif
	printf("execute now day %d part %d on file %s\n", day, part, f);
	clock_t start = clock();
	const char *result = solve(f);
	clock_t end = clock();
	if (result) {
		uint64_t diff = end - start;
		printf("the result is %s\n"
				"  I needed %"I64"u.%.6"I64"u seconds\n", result,
				diff / CLOCKS_PER_SEC,
				((diff % CLOCKS_PER_SEC) * UINT64_C(1000000)) / CLOCKS_PER_SEC);
	} else {
		puts("there is no result");
	}
	return EXIT_SUCCESS;
}
