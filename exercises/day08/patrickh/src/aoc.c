/*
 * aoc.c
 *
 *  Created on: Dec 2, 2024
 *      Author: pat
 */

#include "aoc.h"

#include "color.h"
#include "hash.h"
#include "interactive.h"

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

#ifdef INTERACTIVE
#define INTERACT(...) __VA_ARGS__
#else
#define INTERACT(...)
#endif

struct data* read_data(const char *path);

int year = 2025;
int day = 8;
int part = 2;
FILE *solution_out;
int is_test_data = 0;
#ifdef INTERACTIVE
int interactive = 0;
#else
#define interactive 0
#endif

#define starts_with(str, start) !memcmp(str, start, sizeof(start) - 1)

typedef size_t idx;
typedef ssize_t pos;

#define NUM_MAX UINT64_MAX
typedef uint64_t num;

#define JBC_MAX SSIZE_MAX
typedef pos jbc;
struct junction_box {
	jbc x, y, z;
};

struct connection {
	idx a, b;
	long double distance;
};

struct data {
	size_t box_count;
	size_t box_alloc;
	struct junction_box *boxes;
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

static void print(FILE *str, struct data *data, uint64_t result,
		struct connection *conns, size_t conns_size) {
	if (!do_print && !interactive)
		return;
	if (result || 1)
		fprintf(str, "%sresult=%"I64"u\n%s", STEP_HEADER, result, STEP_BODY);
	for (idx i = 0; i < conns_size; ++i) {
		struct junction_box *a = data->boxes + conns[i].a;
		struct junction_box *b = data->boxes + conns[i].b;
		long double dist = conns[i].distance;
		fprintf(str, "conn[%3"I64"u]: (%6"I64"d,%6"I64"d,%6"I64"d) <== "
		/*		*/"%-7.6Lg ==> (%6"I64"d,%6"I64"d,%6"I64"d)\n", (uint64_t) i,
				(int64_t) a->x, (int64_t) a->y, (int64_t) a->z, dist,
				(int64_t) b->x, (int64_t) b->y, (int64_t) b->z);
	}
	fputs(interactive ? STEP_FINISHED : RESET, str);
}

static int jbeq(const void *a, const void *b) {
	return a == b;
}

static uint64_t jbhs(const void *a) {
	return (uint64_t) a;
}

struct visit_arg0 {
	struct hashset *add;
	struct data *data;
	struct hashset **setps;
};

static int v_visitor(void *arg0, void *entry) {
	struct visit_arg0 *arg = arg0;
	struct junction_box *jb = entry;
	hs_add(arg->add, jb);
	arg->setps[jb - arg->data->boxes] = arg->add;
	return 0;
}

static int cmp_hs(const void *a, const void *b) {
	const struct hashset *sa = a, *sb = b;
	if (sa->entry_count > sb->entry_count)
		return 1;
	else if (sa->entry_count < sb->entry_count)
		return -1;
	return 0;
}

static int v_print(void *arg0, void *entry) {
	struct junction_box *b = entry;
	fprintf(arg0, " (%6"I64"u,%6"I64"u,%6"I64"u)", b->x, b->y, b->z);
	return 0;
}

static uint64_t calc(struct data *data, struct connection *conns,
		size_t conns_size) {
	struct hashset *sets = calloc(data->box_count, sizeof(struct hashset));
	struct hashset **setps = malloc(data->box_count * sizeof(struct hashset*));
	for (idx i = 0; i < data->box_count; ++i) {
		sets[i].equal = jbeq;
		sets[i].hash = jbhs;
		setps[i] = sets + i;
		hs_add(sets + i, data->boxes + i);
	}
	for (idx i = 0; i < conns_size; ++i) {
		struct connection *c = conns + i;
		if (setps[c->a] != setps[c->b]) {
			struct visit_arg0 arg = { setps[c->a], data, setps };
			struct hashset *setb = setps[c->b];
			hs_for_each(setb, &arg, v_visitor);
			hs_clear(setb);
		}
	}
	qsort(sets, data->box_count, sizeof(struct hashset), cmp_hs);
	uint64_t result = 1;
	for (idx i = data->box_count; --i >= 0;) {
		size_t ec = sets[i].entry_count;
		if (!ec)
			break;
		fprintf(solution_out, "  size[%"I64"u]=%"I64"u:",
				data->box_count - i - 1, ec);
		hs_for_each(sets + i, solution_out, v_print);
		fputc('\n', solution_out);
		if (data->box_count - 3 <= i)
			result *= ec;
	}
	free(sets);
	free(setps);
	return result;
}

static uint64_t solve_p1(struct data *data) {
	uint64_t result = 0;
	size_t conn_max_size = is_test_data ? 10 : 1000;
	struct connection *conns = malloc(
			sizeof(struct connection) * conn_max_size);
	size_t conns_size = 0;
	print(solution_out, data, result, conns, conns_size);
	for (idx ai = 0; ai + 1 < data->box_count; ++ai) {
		for (idx bi = ai + 1; bi < data->box_count; ++bi) {
			pos diffx = data->boxes[ai].x - data->boxes[bi].x;
			pos diffy = data->boxes[ai].y - data->boxes[bi].y;
			pos diffz = data->boxes[ai].z - data->boxes[bi].z;
			long double dist = sqrt(
					diffx * diffx + diffy * diffy + diffz * diffz);
			struct connection *low = conns;
			struct connection *high = conns + conns_size - 1;
			while (low <= high) {
				struct connection *mid = low + ((high - low) >> 1);
				if (mid->distance < dist)
					low = mid + 1;
				else if (mid->distance > dist)
					high = mid - 1;
				else {
					low = mid;
					break;
				}
			}
			if (conns_size != conn_max_size)
				conns_size++;

			size_t cpy = conns + conns_size - low - 1;
			if (low != conns + conns_size) {
				memmove(low + 1, low, cpy * sizeof(struct connection));
				low->a = ai;
				low->b = bi;
				low->distance = dist;
				//				if (do_print || interactive)
				//					result = calc(data, conns, conns_size);
				//				print(solution_out, data, result, conns, conns_size);
			}
		}
	}
	result = calc(data, conns, conns_size);
	print(solution_out, data, result, conns, conns_size);
	free(conns);
	return result;
}

struct distarg {
	struct junction_box *other;
	long double dist;
	struct junction_box *result;
};

static int v_dist(void *arg0, void *element) {
	struct distarg *arg = arg0;
	struct junction_box *jb = element;
	if (jb == arg->other)
		return 0;
	pos diffx = jb->x - arg->other->x;
	pos diffy = jb->y - arg->other->y;
	pos diffz = jb->z - arg->other->z;
	long double dist = sqrt(diffx * diffx + diffy * diffy + diffz * diffz);
	if (!arg->result || dist < arg->dist) {
		arg->dist = dist;
		arg->result = jb;
	}
	return 0;
}

struct p2arg {
	struct hashset connected;
	struct hashset unconnected;
	long double dist;
	struct junction_box *min_src;
	struct junction_box *min_dst;
};

static int v_visitp2(void *arg0, void *element) {
	struct p2arg *arg = arg0;
	struct junction_box *jb = element;
	struct hashset *search = &arg->connected;
	if (!arg->connected.entry_count)
		search = &arg->unconnected;
	struct distarg darg = { .other = jb, .dist = arg->dist, .result = arg->min_src };
	hs_for_each(search, &darg, v_dist);
	if (darg.result != arg->min_src) {
		arg->dist = darg.dist;
		arg->min_src = darg.result;
		arg->min_dst = jb;
	}
	return 0;
}

static uint64_t solve_p2(struct data *data) {
	struct p2arg arg = { .connected.equal = jbeq, .connected.hash = jbhs,
			.unconnected.equal = jbeq, .unconnected.hash = jbhs, };
	for (idx i = 0; i < data->box_count; ++i)
		hs_add(&arg.unconnected, data->boxes + i);
	idx i = 0;
	do {
		arg.min_src = NULL;
		arg.min_dst = NULL;
		hs_for_each(&arg.unconnected, &arg, v_visitp2);
		hs_add(&arg.connected, arg.min_dst);
		hs_remove(&arg.unconnected, arg.min_dst);
		fprintf(solution_out,
				"conn[%3"I64"u]: (%6"I64"d,%6"I64"d,%6"I64"d) <== "
				/*		*/"%-7.6Lg ==> (%6"I64"d,%6"I64"d,%6"I64"d) (%"I64"u connected and %"I64"u unconnected)\n",
				(uint64_t) i, (int64_t) arg.min_src->x,
				(int64_t) arg.min_src->y, (int64_t) arg.min_src->z, arg.dist,
				(int64_t) arg.min_dst->x, (int64_t) arg.min_dst->y,
				(int64_t) arg.min_dst->z, (uint64_t) arg.connected.entry_count,
				(uint64_t) arg.unconnected.entry_count);
		++i;
	} while (arg.unconnected.entry_count);
	return arg.min_src->x * arg.min_dst->x;
}

const char* solve(const char *path) {
	struct data *data = read_data(path);
	uint64_t result = 0;
	if (part == 1)
		result = solve_p1(data);
	else
		result = solve_p2(data);
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
	if (data->box_alloc == data->box_count) {
		data->box_alloc += 64;
		data->boxes = reallocarray(data->boxes, data->box_alloc,
				sizeof(struct junction_box));
	}
	char *end;
	long long val = strtoll(line, &end, 10);
	if (val <= 0 || val > JBC_MAX || errno)
		abort();
	data->boxes[data->box_count].x = val;
	if (*end != ',')
		abort();
	val = strtoll(end + 1, &end, 10);
	if (val <= 0 || val > JBC_MAX || errno)
		abort();
	data->boxes[data->box_count].y = val;
	if (*end != ',')
		abort();
	val = strtoll(end + 1, &end, 10);
	if (val <= 0 || val > JBC_MAX || errno)
		abort();
	data->boxes[data->box_count].z = val;
	data->box_count++;
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
			fprintf(stderr,
#ifdef INTERACTIVE
					"usage: %s [[non-]interactive|[no-]print] [p1|p2] [DATA]",
#else
					"usage: %s [non-interactive|[no-]print] [p1|p2] [DATA]",
#endif
					me);
			return 1;
		}
		int idx = 1;
		if (!strcmp("help", argv[idx])) {
			goto print_help;
		}
		if (!strcmp("no-print", argv[idx])) {
			idx++;
			do_print = 0;
			INTERACT(force_non_interactive = 1;)
		} else if (!strcmp("print", argv[idx])) {
			idx++;
			do_print = 1;
			INTERACT(force_non_interactive = 1;)
		} else if (!strcmp("non-interactive", argv[idx])) {
			idx++;
			INTERACT(force_non_interactive = 1;)
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
	} else {
		is_test_data = 1;
		if (!strchr(f, '/')) {
			char *f2 = malloc(64);
			if (snprintf(f2, 64, "rsrc/test%s.txt", f) <= 0) {
				perror("snprintf");
				abort();
			}
			f = f2;
		}
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
