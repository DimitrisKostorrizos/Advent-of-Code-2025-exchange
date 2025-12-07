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
int day = 5;
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

struct id_range {
	id first;
	id last;
};

struct data {
	size_t range_len;
	size_t range_alloc;
	struct id_range *ranges;
	size_t id_len;
	size_t id_alloc;
	id *ids;
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
static void print(FILE *str, struct data *data, uint64_t result) {
	if (result) {
		fprintf(str, "%sresult=%"I64"u\n%s", STEP_HEADER, result, STEP_BODY);
	} else {
		fputs(STEP_BODY, str);
	}
	if (!do_print && !interactive) {
		return;
	}
	fprintf(str, "range count: %"I64"u\n", (uint64_t) data->range_len);
	if (part == 1)
		fprintf(str, "id count: %"I64"u\n", (uint64_t) data->id_len);
	for (int i = 0; i < data->range_len; ++i)
		fprintf(str, "  %"I64"u-%"I64"u\n", (uint64_t) data->ranges[i].first,
				(uint64_t) data->ranges[i].last);
	if (part == 1)
		for (int i = 0; i < data->id_len; ++i)
			fprintf(str, "  %"I64"u\n", (uint64_t) data->ids[i]);
	fputs(interactive ? STEP_FINISHED : RESET, str);
}
#endif

static int is_good(struct data *data, id id);
static int id_insert_or_contains(id *ids, size_t id_len, id new, int insert);

static void print_space(FILE *str, uint64_t count) {
	uint64_t val;
	for (val = 0; val + INT_MAX < count; val += INT_MAX)
		fprintf(str, "%*s", INT_MAX, "");
	fprintf(str, "%*s", (int) (count - val), "");
}

static void print_world(FILE *str, struct data *data, uint64_t result,
		struct id_range *inserted) {
	fprintf(str, "%sresult=%"I64"u\n%s", STEP_HEADER, result, STEP_BODY);
	if (!do_print && !interactive)
		return;
	uint64_t max = data->range_len ? data->ranges[data->range_len - 1].last : 0;
	if (data->id_len && max < data->ids[data->id_len - 1])
		max = data->ids[data->id_len - 1];
	if (max >= 16 && max < 32)
		max = 32;
	else
		max <<= 1;
	int len = log10(max) + 1;
	if (len >= 20)
		abort();
	if (len >= 5 && !getenv("ALLOW_LARGE_PRINT")) {
		fprintf(stderr, "max id too large for you: %d:%"I64"u\n", len, max);
		abort();
	}
	if (len < 2)
		len = 2;
	uint64_t pow = 1;
	for (int i = 1; i < len; ++i)
		pow *= 10;
	for (uint64_t lpow; pow; pow = lpow) {
		fputs(FC_GRAY, str);
		lpow = pow / 10;
		for (id val = 0; val <= max; ++val) {
			int kid = id_insert_or_contains(data->ids, data->id_len, val, 0);
			if (kid)
				fputs(is_good(data, val) ? FC_GREEN : FC_RED, str);
			if ((kid || !val || !(val & (val - 1))) && (val >= pow || pow == 1))
				fputc('0' + (val / pow) % 10, str);
			else
				fputc(' ', str);
			if (kid)
				fputs(FC_GRAY, str);
		}
		fputc('\n', str);
	}
	if (data->id_len) {
		enum print_id_mode {
			mode_none, mode_good, mode_bad
		};
		enum print_id_mode mode = mode_none;
		id last = 0;
		for (idx i = 0; i < data->id_len; ++i) {
			id next = data->ids[i];
			print_space(str, next - last);
			if (is_good(data, data->ids[i])) {
				if (mode != mode_good) {
					mode = mode_good;
					fputs(FC_GREEN, str);
				}
			} else if (mode != mode_bad) {
				mode = mode_bad;
				fputs(FC_RED, str);
			}
			fputs("\u2502", str);
			last = next + 1;
		}
	}
	fputc('\n', str);
	id last = 0;
	for (idx i = 0, oi = 0;; ++i) {
		if (i == data->range_len) {
			for (; oi != data->id_len && data->ids[oi] < last; ++oi)
				;
			for (; oi != data->id_len; ++oi) {
				id cur = data->ids[oi];
				print_space(str, cur - last);
				fputs(FC_RED"\U0001F5D9"RESET, str);
				last = cur + 1;
			}
			break;
		}
		id next = data->ranges[i].first;
		for (; oi < data->id_len && data->ids[oi] < next; ++oi) {
			id cur = data->ids[oi];
			if (cur >= last) {
				print_space(str, cur - last);
				fputs(FC_RED"\U0001F5D9"RESET, str);
				last = cur + 1;
			}
		}
		if (next > last)
			print_space(str, next - last);
		else
			next = last;
		id end = data->ranges[i].last;
		if (i + 1 < data->range_len && end >= data->ranges[i + 1].first) {
			end = data->ranges[i + 1].first - 1;
		}
		for (id id = next; id <= end; ++id) {
			if (inserted && id >= inserted->first && id <= inserted->last) {
				fputs(BC_RED FC_GRAY"\u2592"RESET, str);
			} else if (id_insert_or_contains(data->ids, data->id_len, id, 0)) {
				fputs(REVERSE_FB BOLD BC_RGB(0,127,0)"\u2713"RESET, str);
			} else {
				fputs("\u2588", str);
			}
		}
		if (end != data->ranges[i].last) {
			fputs(BC_RED FC_GRAY, str);
			for (id id = end + 1; id <= data->ranges[i].last; ++id)
				fputs("\u2592", str);
			fputs(RESET, str);
		}
		last = data->ranges[i].last + 1;
	}
	fputc('\n', str);
	fputs(interactive ? STEP_FINISHED : RESET, str);
}

static uint64_t range_insert_or_contains(struct data *data,
		struct id_range *new, uint64_t result) {
	if (result != UINT64_MAX)
		result += new->last - new->first + 1;
	struct id_range *low = data->ranges;
	struct id_range *high = data->ranges + data->range_len - 1;
	while (low <= high) {
		struct id_range *mid = low + ((high - low) >> 1);
		if (mid->last < new->first)
			low = mid + 1;
		else if (mid->first > new->last)
			high = mid - 1;
		else if (result != UINT64_MAX) {
			if (mid->first > new->first)
				mid->first = new->first;
			if (mid->last < new->last)
				mid->last = new->last;
			if (mid > data->ranges && mid[-1].last >= mid->first) {
				print_world(solution_out, data, result, new);
				result -= mid[-1].last - mid[1].first + 1;
				if (mid[-1].last < mid->last)
					mid[-1].last = mid->last;
				if (mid[-1].first > mid->first)
					mid[-1].first = mid->first;
				memmove(mid - 1, mid,
						(data->ranges + data->range_len - mid)
								* sizeof(struct id_range));
				--data->range_len;
				--mid;
			}
			if (mid + 1 < data->ranges + data->range_len
					&& mid[1].first <= mid->last) {
				print_world(solution_out, data, result, new);
				result -= mid[1].last - mid[1].first + 1;
				if (mid[1].last < mid->last)
					mid[1].last = mid->last;
				if (mid[1].first > mid->first)
					mid[1].first = mid->first;
				memmove(mid, mid + 1,
						(data->ranges + data->range_len - mid)
								* sizeof(struct id_range));
				--data->range_len;
			}
			return result;
		} else
			return 143;
	}
	if (result != UINT64_MAX) {
		memmove(low + 1, low,
				(data->ranges + data->range_len - low)
						* sizeof(struct id_range));
		low->first = new->first;
		low->last = new->last;
		++data->range_len;
		return result;
	}
	return 0;
}

static int id_insert_or_contains(id *ids, size_t id_len, id new, int insert) {
	id *low = ids;
	id *high = ids + id_len - 1;
	while (low <= high) {
		id *mid = low + ((high - low) >> 1);
		if (*mid < new)
			low = mid + 1;
		else if (*mid > new)
			high = mid - 1;
		else if (insert)
			abort();
		else
			return 261;
	}
	if (insert) {
		memmove(low + 1, low, (ids + id_len - low) * sizeof(id));
		*low = new;
	}
	return 0;
}

static uint64_t rebuild(struct data *data) {
	uint64_t result = 0;
	size_t r_len = data->range_len;
	struct id_range *ranges = data->ranges;
	data->range_len = 0;
	data->range_alloc = r_len;
	data->ranges = malloc(sizeof(struct id_range) * r_len);
	size_t i_len = data->id_len;
	id *ids = data->ids;
	data->id_alloc = i_len;
	data->id_len = 0;
	for (idx i = 0; i < r_len; ++i) {
		result = range_insert_or_contains(data, ranges + i, result);
		print_world(solution_out, data, result, NULL);
	}
	free(ranges);
	if (interactive || do_print) {
		data->ids = malloc(sizeof(id) * i_len);
		for (idx i = 0; i < i_len; ++i) {
			id_insert_or_contains(data->ids, data->id_len++, ids[i], 1);
			print_world(solution_out, data, result, NULL);
		}
	}
	free(ids);
	return result;
}

static int is_good(struct data *data, id id) {
	struct id_range new = { id, id };
	return range_insert_or_contains(data, &new, UINT64_MAX);
}

const char* solve(const char *path) {
	struct data *data = read_data(path);
	uint64_t result = 0;
	result = rebuild(data);
	if (part == 1) {
		result = 0;
		for (idx ii = 0; ii < data->id_len; ++ii) {
			if (is_good(data, data->ids[ii])) {
				result++;
			}
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
	}
	if (data->range_len == data->range_alloc) {
		data->range_alloc += 64;
		data->ranges = reallocarray(data->ranges, data->range_alloc,
				sizeof(struct id_range));
	}
	if (data->id_len == data->id_alloc) {
		data->id_alloc += 64;
		data->ids = reallocarray(data->ids, data->id_alloc,
				sizeof(struct id_range));
	}
	char *end;
	id val = strtoll(line, &end, 10);
	if (val < 0 || line == end)
		abort();
	if (*end == '-') {
		data->ranges[data->range_len].first = val;
		char *chr = end + 1;
		val = strtoll(chr, &end, 10);
		if (val < 0 || chr == end)
			abort();
		data->ranges[data->range_len++].last = val;
	} else {
		data->ids[data->id_len++] = val;
	}
	if (errno)
		(perror("strtol"), abort());
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
