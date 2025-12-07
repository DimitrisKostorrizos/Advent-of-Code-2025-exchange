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
int day = 3;
int part = 2;
FILE *solution_out;
#ifdef INTERACTIVE
int interactive = 0;
#else
#define interactive 0
#endif

#define starts_with(str, start) !memcmp(str, start, sizeof(start) - 1)

#define IDX_MAX SIZE_MAX
typedef size_t idx;
typedef off_t pos;

struct data {
	size_t len;
	size_t alloc;
	char **batteries;
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

static void print(FILE *str, struct data *data, uint64_t result) {
	if (result) {
		fprintf(str, "%sresult=%"I64"u\n%s", STEP_HEADER, result, STEP_BODY);
	} else {
		fputs(STEP_BODY, str);
	}
	if (!do_print && !interactive) {
		return;
	}
	fputs(interactive ? STEP_FINISHED : RESET, str);
}

static void print_batteries(FILE *str, uint64_t result, char *batteries,
		idx idx0, idx idx1) {
	if (result) {
		fprintf(str, "%sresult=%"I64"u\n%s", STEP_HEADER, result, STEP_BODY);
	} else {
		fputs(STEP_BODY, str);
	}
	if (!do_print && !interactive) {
		return;
	}
	if (idx0 > INT_MAX || idx1 - idx0 - 1 > INT_MAX || idx1 < idx0)
		abort();
	unsigned val0 = batteries[idx0];
	unsigned val1 = batteries[idx1];
	unsigned sum = (val0 - '0') * 10 + val1 - '0';
	fprintf(str, "%.*s"BOLD"%c"DEF_INTENSE"%.*s"BOLD"%c"DEF_INTENSE"%s\n"
	/*		*/"idx0=%"I64"u; val0=%c\n"
	/*		*/"idx1=%"I64"u; val1=%c\n"
	/*		*/"sum: %u\n", (int) idx0, batteries, val0, (int) (idx1 - idx0 - 1), batteries + idx0 + 1,
			val1, batteries + idx1 + 1, (uint64_t) idx0, val0, (uint64_t) idx1,
			val1, sum);
	fputs(interactive ? STEP_FINISHED : RESET, str);
}

static void print_batteries_p2(FILE *str, uint64_t result, char *batteries,
		idx idx[12], char *val) {
	if (result || 1)
		fprintf(str, "%sresult=%"I64"u\n", STEP_HEADER, result);
	else
		fputs(STEP_HEADER, str);
	if (!do_print && !interactive)
		return;
	char *last = batteries;
	for (int i = 0; i < 12 && (!i || idx[i]); ++i) {
		char *next = batteries + idx[i];
		if (next - last > INT_MAX)
			abort();
		fprintf(str, FC_GRAY"%.*s"RESET BOLD"%c"RESET, (int) (next - last),
				last, (unsigned) *next);
		last = next + 1;
	}
	fprintf(str, FC_GRAY"%s"RESET"\n%s", last, STEP_BODY);
	for (int i = 0; i < 12; ++i) {
		if (!i || idx[i])
			fprintf(str, "%2d: ([%2"I64"u]=%c)\n", i, idx[i],
					(unsigned) batteries[idx[i]]);
		else if (interactive)
			fprintf(str, "%2d:\n", i);
		else
			break;
	}
	fprintf(str, "%svalue: %s\n", STEP_FOOTER, val);
	fputs(interactive ? STEP_FINISHED : RESET, str);
}

static uint64_t solve_step(char *batteries, uint64_t result) {
	idx max0_idx = IDX_MAX, max1_idx = IDX_MAX;
	unsigned max0_val = '\0', max1_val = '\0';
	for (char *i = batteries; i[1]; ++i) {
		if (*i > max0_val) {
			max0_val = *i;
			max0_idx = i - batteries;
		}
	}
	for (char *i = batteries + max0_idx + 1; *i; ++i) {
		if (*i > max1_val) {
			max1_val = *i;
			max1_idx = i - batteries;
		}
	}
	result += (max0_val - '0') * 10 + max1_val - '0';
	print_batteries(solution_out, result, batteries, max0_idx, max1_idx);
	return result;
}

static uint64_t solve_step_p2(char *batteries, uint64_t result) {
	idx max_idx[12] = { 0 };
	char max_val[13] = { 0 };
	_Static_assert((idx) 0 - (idx) 1 > (idx) 0, "Error!");
	for (int mi = 0; mi < 12; ++mi) {
		for (char *i = batteries + (mi ? max_idx[mi - 1] + 1 : 0);
				i[11 - mi]; ++i) {
			if (*i > max_val[mi]) {
				max_val[mi] = *i;
				max_idx[mi] = i - batteries;
				print_batteries_p2(solution_out, result, batteries, max_idx, max_val);
			}
		}
	}
	long long int sum = strtoll(max_val, NULL, 10);
	result += sum;
	print_batteries_p2(solution_out, result, batteries, max_idx, max_val);
	return result;
}

const char* solve(const char *path) {
	struct data *data = read_data(path);
	uint64_t result = 0;
	if (part == 1) {
		for (idx i = 0; i < data->len; ++i)
			result = solve_step(data->batteries[i], result);
	} else {
		for (idx i = 0; i < data->len; ++i)
			result = solve_step_p2(data->batteries[i], result);
	}
	print(solution_out, data, result);
	free(data);
	return u64toa(result);
}

static struct data* parse_line(struct data *data, char *line) {
	for (; *line && isspace(*line); ++line)
		;
	if (!*line)
		return data;
	if (!data)
		data = calloc(1, sizeof(struct data));
	if (data->alloc == data->len) {
		data->alloc += 64;
		data->batteries = realloc(data->batteries, data->alloc * sizeof(char*));
	}
	char *end;
	for (end = line; *end && isdigit(*end); ++end)
		;
	char*endend;
	for (endend = end; *endend && isspace(*endend); ++endend)
		;
	if (*endend)
		abort();
	*end = '\0';
	data->batteries[data->len++] = strdup(line);
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
	solution_out = stdout;
	char *me = argv[0];
	char *f = 0;
	if (argc > 1) {
#ifdef INTERACTIVE
		if (argc > 4)
#else
		if (argc > 3)
#endif
				{
			print_help: ;
			fprintf(stderr, "usage: %s"
#ifdef INTERACTIVE
							" [interactive]"
#endif
							" [p1|p2] [DATA]\n", me);
			return 1;
		}
		int idx = 1;
		if (!strcmp("help", argv[idx])) {
			goto print_help;
		}
#ifdef INTERACTIVE
		if (!strcmp("interactive", argv[idx])) {
			idx++;
			interactive = 1;
		}
		if (idx < argc)
#endif
		{
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
	interact(f, interactive);
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
