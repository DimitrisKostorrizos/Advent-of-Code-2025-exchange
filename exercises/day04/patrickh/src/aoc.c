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
int day = 4;
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

typedef uint64_t id;


struct data {
	size_t line_len;
	size_t line_count;
	size_t line_alloc;
	char *world;
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

static void print(FILE *str, struct data *data, uint64_t result, uint64_t add_result) {
	if (result || 1) {
		fprintf(str, "%sresult=%"I64"u\n", STEP_HEADER, result);
	} else {
		fputs(STEP_HEADER, str);
	}
	if (!do_print && !interactive) {
		return;
	}
	fprintf(str, "Remove %"I64"u rolls of paper\n%s", add_result, STEP_BODY);
	for (idx l = 0; l < data->line_count; ++l) {
		char *line = data->world + (l * data->line_len);
		char *end = line + data->line_len;
		char last = '.';
		for (; line < end; ++line) {
			if (*line != last) {
				last = *line;
				switch (last) {
				case '@':
					fputs(RESET FC_YELLOW, str);
					break;
				case 'x':
					fputs(RESET FC_RED, str);
					break;
				case '.':
					fputs(RESET, str);
					break;
				}
			}
			fputc(last, str);
		}
		fputc('\n', str);
	}
	fputs(interactive ? STEP_FINISHED : RESET, str);
}

static int is_match(struct data *data, idx l, idx c) {
	_Static_assert((idx) 0 - (idx) 1 > 0, "Error!");
	return l < data->line_count && c < data->line_len
			&& data->world[l * data->line_len + c] != '.';
}

static int count(struct data *data, idx l, idx c) {
	int result = 0;
	result += is_match(data, l, c + 1);
	result += is_match(data, l, c - 1);
	result += is_match(data, l + 1, c + 1);
	result += is_match(data, l + 1, c - 1);
	result += is_match(data, l + 1, c);
	result += is_match(data, l - 1, c + 1);
	result += is_match(data, l - 1, c - 1);
	result += is_match(data, l - 1, c);
	return result;
}

static uint64_t mark_removable(struct data *data) {
	uint64_t result = 0;
	for (idx l = 0; l < data->line_count; ++l)
		for (idx c = 0; c < data->line_len; ++c)
			if (data->world[l * data->line_len + c] == '@'
					&& count(data, l, c) < 4) {
				result++;
				data->world[l * data->line_len + c] = 'x';
			}
	return result;
}

const char* solve(const char *path) {
	struct data *data = read_data(path);
	uint64_t result = 0;
	print(solution_out, data, result, 0);
	result = mark_removable(data);
	print(solution_out, data, result, result);
	if (part == 2) {
		while (__LINE__) {
			char *chr = data->world, *end = data->world
					+ data->line_len * data->line_count;
			while (136) {
				chr = memchr(chr, 'x', end - chr);
				if (!chr)
					break;
				*chr = '.';
			}
			uint64_t add_result = mark_removable(data);
			print(solution_out, data, result, add_result);
			if (!add_result)
				break;
			result += add_result;
		}
	}
	free(data);
	return u64toa(result);
}

static struct data* parse_line(struct data *data, char *line) {
	for (; *line && isspace(*line); ++line)
		;
	if (!*line)
		return data;
	if (!data) {
		data = calloc(1, sizeof(struct data));
		char *end = line + strlen(line);
		while (isspace(*--end))
			;
		data->line_len = end - line + 1;
	}
	if (data->line_alloc == data->line_count) {
		data->line_alloc += 64;
		data->world = reallocarray(data->world, data->line_alloc, data->line_len);
	}
	for (idx i = 0; i < data->line_len; ++i)
		if (line[i] != '.' && line[i] != '@')
			abort();
	for (char *end = line + data->line_len; *end; ++end)
		if (!isspace(*end))
			abort();
	memcpy(data->world + (data->line_count++ * data->line_len), line,
			data->line_len);
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
