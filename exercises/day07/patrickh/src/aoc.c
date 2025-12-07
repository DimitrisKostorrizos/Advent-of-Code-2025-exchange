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

#define NUM_MAX UINT64_MAX
typedef uint64_t num;

struct data {
	size_t line_count;
	size_t line_length;
	char *world;
	num *counts;
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

#if 0
static void print_space(FILE *str, uint64_t count) {
	uint64_t val;
	for (val = 0; val + INT_MAX < count; val += INT_MAX)
		fprintf(str, "%*s", INT_MAX, "");
	fprintf(str, "%*s", (int) (count - val), "");
}
#endif

static void print(FILE *str, struct data *data, uint64_t result, num left, num right, num old) {
	if (result || 1)
		fprintf(str, "%sresult=%"I64"u\n", STEP_HEADER, result);
	if (!do_print && !interactive)
		return;
	if (right)
		fprintf(str, "%"I64"u->%"I64"u+%"I64"u\n%s", (uint64_t) old,
				(uint64_t) left, (uint64_t) right, STEP_BODY);
	else if (left)
		fprintf(str, "%"I64"u->%"I64"u\n%s", (uint64_t) old, (uint64_t) left,
				STEP_BODY);
	else
		fputs(STEP_BODY, str);
	for (idx l = 0; l < data->line_count; ++l) {
		fwrite(data->world + l * data->line_length, 1, data->line_length, str);
		fputc('\n', str);
	}
	fputs(interactive ? STEP_FINISHED : RESET, str);
}

const char* solve(const char *path) {
	struct data *data = read_data(path);
	uint64_t result = part == 2;
	print(solution_out, data, result, 0, 0, 0);
	char *a = memchr(data->world, 'S', data->line_length);
	if (part == 2) {
		data->counts = calloc(data->line_count * data->line_length,
				sizeof(num));
		data->counts[a - data->world] = 1;
	}
	char *end = data->world + data->line_length * data->line_count;
	do {
		if (a[data->line_length] == '^') {
			if (a[data->line_length + 1] != '.')
				abort();
			if (a[data->line_length - 1] != '.'
					&& a[data->line_length - 2] != '+' && a[-1] != '|')
				abort();
			if (a < end - data->line_length) {
				a[data->line_length - 1] = '|';
				a[data->line_length + 1] = '|';
				a[data->line_length] = '+';
			}
			if (part == 1) {
				++result;
				print(solution_out, data, result, 0, 0, 0);
			} else {
				num cur = data->counts[a - data->world];
				result += cur;
				if (a < end - data->line_length) {
					if ((data->counts[a - data->world + data->line_length - 1] +=
							cur) < cur)
						abort();
					if (data->counts[a - data->world + data->line_length + 1])
						abort();
					data->counts[a - data->world + data->line_length + 1] = cur;
				}
				print(solution_out, data, result,
						data->counts[a - data->world + data->line_length - 1],
						cur,
						cur);
			}
		} else if (a < end - data->line_length) {
			a[data->line_length] = '|';
			if (part == 2) {
				num cur = data->counts[a - data->world];
				data->counts[a - data->world + data->line_length] += cur;
				print(solution_out, data, result,
						data->counts[a - data->world + data->line_length], 0,
						cur);
			} else {
				print(solution_out, data, result, 0, 0, 0);
			}
		}
		if (part == 1) {
			a = memchr(a + 1, '|', end - a - 1);
		} else {
			for (++a; a != end && !data->counts[a - data->world]; ++a)
				;
		}
	} while (part == 1 ? !!a : a != end);
	print(solution_out, data, result, 0, 0, 0);
	free(data);
	return u64toa(result);
}

static struct data* parse_line(struct data *data, char *line) {
	for (; *line && isspace(*line); ++line)
		;
	if (!*line)
		return data;
	char *end = line;
	for (; *end; ++end)
		if (*end != '.' && *end != '^' && (data || *end != 'S'))
			break;
	if (!data) {
		data = calloc(1, sizeof(struct data));
		data->line_length = end - line;
		data->world = malloc((data->line_length + 1) * data->line_length);
	}
	memcpy(data->world + data->line_count * data->line_length, line,
			end - line);
	if (data->line_length != end - line
			|| data->line_count++ > data->line_length)
		abort();
	for (; *end && isspace(*end); ++end)
		;
	if (*end)
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
#ifdef INTERACTIVE
			fprintf(stderr, "usage: %s [[non-]interactive|[no-]print] [p1|p2] [DATA]", me);
#else
			fprintf(stderr, "usage: %s [non-interactive|[no-]print] [p1|p2] [DATA]", me);

#endif
			return 1;
		}
		int idx = 1;
		if (!strcmp("help", argv[idx])) {
			goto print_help;
		}
		if (!strcmp("no-print", argv[idx])) {
			idx++;
			do_print = 0;
		} else if (!strcmp("print", argv[idx])) {
			idx++;
			do_print = 1;
		} else if (!strcmp("non-interactive", argv[idx])) {
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
	if (result)
		printf("the result is %s\n", result);
	else
		puts("there is no result");
	uint64_t diff = end - start;
	printf("  I needed %"I64"u.%.6"I64"u seconds\n", diff / CLOCKS_PER_SEC,
			((diff % CLOCKS_PER_SEC) * UINT64_C(1000000)) / CLOCKS_PER_SEC);
	return EXIT_SUCCESS;
}
