/*
 * aoc.c
 *
 *  Created on: Dec 2, 2024
 *      Author: pat
 */

#include "aoc.h"

#include <errno.h>
#include <stdarg.h>
#include <stdint.h>
#include <stdio.h>
#include <ctype.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#include "color.h"
#include "hash.h"
#include "interactive.h"

#ifdef INTERACTIVE
#define INTERACT(...) __VA_ARGS__
#else
#define INTERACT(...)
#endif

struct data* read_data(const char *path);

int day = 1;
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

#define MAX_ROTATION 0x7FFF
struct rotation {
	uint16_t width :15;
	uint16_t left :1;
};

struct data {
	size_t len;
	size_t alloc;
	struct rotation *rotations;
};

static int do_print = 1;

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

static pos solve_step(struct rotation *r, pos dial, uint64_t *result) {
	int old = dial;
	uint64_t hit_cnt = 0;
	if (part == 1) {
		if (r->left)
			dial -= r->width;
		else
			dial += r->width;
		while (dial < 0)
			dial += 100;
		if (dial >= 100)
			dial %= 100;
		if (!dial)
			hit_cnt = 1;
	} else {
		pos m = r->width;
		if (m >= 100) {
			hit_cnt += m / 100;
			m = m % 100;
		}
		if (r->left)
			m = -m;
		pos new = dial + m;
		int hit = 0;
		if (new < 0) {
			if (dial)
				hit = 1;
			dial = new + 100;
		} else if (new >= 100) {
			dial = new - 100;
			hit = 1;
		} else {
			dial = new;
		}
		if (!dial)
			hit = 1;
		hit_cnt += hit;
	}
	(*result) += hit_cnt;
	if (hit_cnt > 1 || (hit_cnt == 1 && dial)) {
		static char times[31] = "once";
		if (hit_cnt != 1) {
			sprintf(times, "%"I64"u times", hit_cnt);
		}
		print_step(solution_out, *result,
				"The dial is rotated %c%d to point at %d; during this rotation, it points at 0 %s.\n"
						"  %d%+d=%d (%"I64"u)\n", r->left ? 'L' : 'R', //
				(int) r->width, (int) dial, times, old,
				r->left ? -(int) r->width : (int) r->width, (int) dial,
				hit_cnt);
	} else
		print_step(solution_out, *result,
				"The dial is rotated %c%d to point at %d.\n"
						"  %d%+d=%d (%"I64"u)\n", r->left ? 'L' : 'R', (int) r->width,
				(int) dial, old, r->left ? -(int) r->width : (int) r->width,
				(int) dial, hit_cnt);
	return dial;
}

const char* solve(const char *path) {
	struct data *data = read_data(path);
	uint64_t result = 0;
	pos dial = 50;
	print_step(solution_out, result, "The dial starts by pointing at %"I64"u\n",
			dial);
	for (idx i = 0; i < data->len; ++i) {
		dial = solve_step(data->rotations + i, dial, &result);
	}
	print(solution_out, data, result);
	free(data);
	return u64toa(result);
}

static struct data* parse_line(struct data *data, char *line) {
	for (; *line && isspace(*line); ++line)
		;
	if (!*line) {
		return data;
	}
	if (!data) {
		data = calloc(1, sizeof(struct data));
	}
	if (data->alloc == data->len) {
		data->alloc += 64;
		data->rotations = realloc(data->rotations,
				data->alloc * sizeof(struct rotation));
	}
	int left = 0;
	if (*line == 'L')
		left = 1;
	else if (*line != 'R')
		abort();
	char *end;
	long val = strtol(line + 1, &end, 10);
	if (val <= 0 || val > MAX_ROTATION)
		abort();
	data->rotations[data->len].left = left;
	data->rotations[data->len].width = val;
	data->len++;
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
